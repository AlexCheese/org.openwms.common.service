/*
 * Copyright 2005-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.common.location;

import org.ameba.exception.BusinessRuntimeException;
import org.ameba.exception.NotFoundException;
import org.ameba.http.MeasuredRestController;
import org.ameba.i18n.Translator;
import org.openwms.common.location.api.ErrorCodeVO;
import org.openwms.common.location.api.LocationGroupMode;
import org.openwms.common.location.api.LocationGroupState;
import org.openwms.common.location.api.LockMode;
import org.openwms.common.location.api.LockType;
import org.openwms.common.location.api.events.TargetEvent;
import org.openwms.core.http.AbstractWebController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.lang.String.format;
import static org.openwms.common.CommonMessageCodes.LOCATION_ID_INVALID;
import static org.openwms.common.CommonMessageCodes.LOCK_MODE_UNSUPPORTED;
import static org.openwms.common.CommonMessageCodes.LOCK_TYPE_UNSUPPORTED;
import static org.openwms.common.CommonMessageCodes.TARGET_NOT_SUPPORTED;
import static org.openwms.common.location.api.LocationApiConstants.API_TARGETS;

/**
 * A TargetController represents the REST API to handle the state and availability of {@code Target}s.
 *
 * @author Heiko Scherrer
 */
@Profile("!INMEM")
@MeasuredRestController
class TargetController extends AbstractWebController {

    private final Translator translator;
    private final LocationService locationService;
    private final LocationGroupService locationGroupService;
    private final ApplicationContext ctx;

    TargetController(Translator translator, LocationService locationService, LocationGroupService locationGroupService,
                     ApplicationContext ctx) {
        this.translator = translator;
        this.locationService = locationService;
        this.locationGroupService = locationGroupService;
        this.ctx = ctx;
    }

    /**
     * Change the current {@code mode} a {@code Target}, identified by {@code targetBK} operates in.
     *
     * @param targetBK The business key of the Target, can be a {@code LocationPK} in String format or a LocationGroup name
     * @param type The type of lock to apply to the Target
     * @param mode The mode to apply to the Targets lock
     */
    @PostMapping(path = API_TARGETS + "/{targetBK}", params = {"type!=PERMANENT_LOCK", "mode"})
    public void changeState(
            @PathVariable("targetBK") String targetBK,
            @RequestParam("type") LockType type,
            @RequestParam("mode") LockMode mode
    ) {

            if (LocationPK.isValid(targetBK)) {

                Location location = locationService.findByLocationId(targetBK).orElseThrow(() -> locationNotFound(targetBK));
                switch(type) {
                    case ALLOCATION_LOCK:
                        changeLocation(
                                mode,
                                location,
                                (l, code) -> locationService.changeState(l.getPersistentKey(), code)
                        );
                        break;
                    case OPERATION_LOCK:
                        throw new UnsupportedOperationException("Changing the operation mode of Locations is currently not supported in the API");
                    default:
                        unsupportedOperation(type);
                }
            } else {

                Optional<LocationGroup> optLG = locationGroupService.findByName(targetBK);
                if (optLG.isPresent()) {

                    switch(type) {
                        case ALLOCATION_LOCK:
                            changeLocationGroupState(
                                    mode,
                                    optLG.get(),
                                    (lg, states) -> locationGroupService.changeGroupState(lg.getPersistentKey(), states[0], states[1])
                            );
                            break;
                        case OPERATION_LOCK:
                            changeLocationGroupMode(
                                    mode,
                                    optLG.get(),
                                    (lg, m) -> locationGroupService.changeOperationMode(lg.getName(), m)
                            );
                            break;
                        default:
                            unsupportedOperation(type);
                    }
                } else {
                    targetNotFound(targetBK);
                }
            }
    }

    private void targetNotFound(String targetBK) {
        throw new NotFoundException(translator, TARGET_NOT_SUPPORTED, new String[]{targetBK}, targetBK);
    }

    private void changeLocation(LockMode mode, Target target, BiConsumer<Target, ErrorCodeVO> fnc) {
        switch(mode) {
            case IN:
                fnc.accept(target, ErrorCodeVO.LOCK_STATE_IN);
                break;
            case OUT:
                fnc.accept(target, ErrorCodeVO.LOCK_STATE_OUT);
                break;
            case IN_AND_OUT:
                fnc.accept(target, ErrorCodeVO.LOCK_STATE_IN_AND_OUT);
                break;
            case NONE:
                fnc.accept(target, ErrorCodeVO.UNLOCK_STATE_IN_AND_OUT);
                break;
            default:
                unsupportedOperation(mode);
        }
    }

    private void changeLocationGroupState(LockMode mode, Target target, BiConsumer<Target, LocationGroupState[]> fnc) {
        switch(mode) {
            case IN:
                fnc.accept(target, new LocationGroupState[]{LocationGroupState.NOT_AVAILABLE, LocationGroupState.AVAILABLE});
                break;
            case OUT:
                fnc.accept(target, new LocationGroupState[]{LocationGroupState.AVAILABLE, LocationGroupState.NOT_AVAILABLE});
                break;
            case IN_AND_OUT:
                fnc.accept(target, new LocationGroupState[]{LocationGroupState.NOT_AVAILABLE, LocationGroupState.NOT_AVAILABLE});
                break;
            case NONE:
                fnc.accept(target, new LocationGroupState[]{LocationGroupState.AVAILABLE, LocationGroupState.AVAILABLE});
                break;
            default:
                unsupportedOperation(mode);
        }
    }

    private void changeLocationGroupMode(LockMode mode, LocationGroup target, BiConsumer<LocationGroup, String> fnc) {
        switch(mode) {
            case IN:
                fnc.accept(target, LocationGroupMode.OUTFEED);
                break;
            case OUT:
                fnc.accept(target, LocationGroupMode.INFEED);
                break;
            case IN_AND_OUT:
                fnc.accept(target, LocationGroupMode.NO_OPERATION);
                break;
            case NONE:
                fnc.accept(target, LocationGroupMode.INFEED_AND_OUTFEED);
                break;
            default:
                unsupportedOperation(mode);
        }
    }

    private NotFoundException locationNotFound(String targetBK) {
        return new NotFoundException(translator, LOCATION_ID_INVALID, new String[]{targetBK}, targetBK);
    }

    private void unsupportedOperation(LockMode mode) {
        throw new BusinessRuntimeException(translator, LOCK_MODE_UNSUPPORTED, new Serializable[]{mode}, mode);
    }

    private void unsupportedOperation(LockType type) {
        throw new BusinessRuntimeException(translator, LOCK_TYPE_UNSUPPORTED, new Serializable[]{type}, type);
    }

    /**
     * Lock the {@code Target} identified by {@code targetBK}.
     *
     * @param targetBK The business key of the Target, can be a {@code LocationPK} in String format or a LocationGroup name
     * @param reAllocation If {@literal true} open outfeed orders will be re-allocated
     */
    @PostMapping(path = API_TARGETS + "/{targetBK}", params = {"type=PERMANENT_LOCK", "mode=lock"})
    public void lock(
            @PathVariable("targetBK") String targetBK,
            @RequestParam(value = "reallocation", required = false) Boolean reAllocation
    ) {
        if (LocationPK.isValid(targetBK)) {
            Location location = locationService.findByLocationId(targetBK).orElseThrow(() -> locationNotFound(targetBK));

            // Okay we handle a Location as Target
            locationService.changeState(location.getPersistentKey(), ErrorCodeVO.LOCK_STATE_IN_AND_OUT);
            raiseEvent(targetBK, reAllocation, LockMode.NONE);
            return;
        }

        Optional<LocationGroup> optLG = locationGroupService.findByName(targetBK);
        if (optLG.isPresent()) {

            // The Target is a LocationGroup
            locationGroupService.changeGroupState(optLG.get().getPersistentKey(), LocationGroupState.NOT_AVAILABLE,
                    LocationGroupState.NOT_AVAILABLE);
            locationGroupService.changeOperationMode(targetBK, LocationGroupMode.NO_OPERATION);
            raiseEvent(targetBK, reAllocation, LockMode.NONE);
            return;
        }

        targetNotFound(targetBK);
    }

    /**
     * Unlock or release the {@code Target} identified by {@code targetBK}.
     *
     * @param targetBK The business key of the Target, can be a {@code LocationPK} in String format or a LocationGroup name
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = API_TARGETS + "/{targetBK}", params = {"type=PERMANENT_LOCK", "mode=unlock"})
    public void release(
            @PathVariable("targetBK") String targetBK
    ) {
        if (LocationPK.isValid(targetBK)) {
            Location location = locationService.findByLocationId(targetBK).orElseThrow(() -> locationNotFound(targetBK));

            // Okay we handle a Location as Target
            locationService.changeState(location.getPersistentKey(), ErrorCodeVO.UNLOCK_STATE_IN_AND_OUT);
            raiseEvent(targetBK, null, LockMode.IN_AND_OUT);
            return;
        }

        Optional<LocationGroup> optLG = locationGroupService.findByName(targetBK);
        if (optLG.isPresent()) {

            // The Target is a LocationGroup
            locationGroupService.changeGroupState(optLG.get().getPersistentKey(), LocationGroupState.AVAILABLE,
                    LocationGroupState.AVAILABLE);
            locationGroupService.changeOperationMode(targetBK, LocationGroupMode.INFEED_AND_OUTFEED);
            raiseEvent(targetBK, null, LockMode.IN_AND_OUT);
            return;
        }

        targetNotFound(targetBK);
    }

    private void raiseEvent(String targetBK, Boolean reAllocation, LockMode mode) {
        ctx.publishEvent(
                TargetEvent
                        .newBuilder()
                        .targetBK(targetBK)
                        .lockType(LockType.PERMANENT_LOCK)
                        .operationMode(mode)
                        .reAllocation(reAllocation)
                        .build()
        );
    }
}
