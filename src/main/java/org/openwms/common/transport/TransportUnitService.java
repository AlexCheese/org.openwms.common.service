/*
 * Copyright 2005-2020 the original author or authors.
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
package org.openwms.common.transport;

import org.openwms.common.location.LocationPK;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A TransportService offers functionality to create, read, update and delete
 * {@link TransportUnit}s. Additionally it defines useful methods regarding the general
 * handling with {@link TransportUnit}s.
 *
 * @author Heiko Scherrer
 */
public interface TransportUnitService {

    /**
     * Create a new {@link TransportUnit} with the type {@link TransportUnitType} placed
     * on an initial {@code Location}. The new {@link TransportUnit} has the given
     * {@link Barcode} as identifier.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param transportUnitType The type of the TransportUnit
     * @param actualLocation The Location where the TransportUnit is placed on
     * @param strict Whether the implementation shall throw an exception when a
     *               TransportUnit already exists ({@literal true}) or not
     *               ({@literal false}
     * @return The newly created instance
     */
    TransportUnit create(Barcode barcode, TransportUnitType transportUnitType,
            LocationPK actualLocation, Boolean strict);

    /**
     * Create a new {@link TransportUnit} with the {@link TransportUnitType} placed on the
     * given {@code actualLocation}. The new {@link TransportUnit} has the given
     * {@link Barcode} as identifier.
     *
     * @param barcode The business identifier of the TransportUnit, must not be {@literal null}
     * @param transportUnitType The type of the TransportUnit, must not be {@literal null}
     * @param actualLocation The Location where the TransportUnit is placed on, must not be {@literal null}
     * @param strict Whether the implementation shall throw an exception when a
     *               TransportUnit already exists ({@literal true}) or not
     *               ({@literal false}
     * @return The newly created instance
     * @throws org.ameba.exception.ServiceLayerException when invalid parameters
     */
    TransportUnit create(
            @NotNull Barcode barcode,
            @NotEmpty String transportUnitType,
            @NotEmpty String actualLocation,
            Boolean strict);

    /**
     * Take the TransportUnit {@code tu} and try to update it as-is in the persistent
     * storage.
     * <p>
     * The implementation does not require any further checks. Assume that it tries to
     * detach the entity class with the persistence context and save the given state.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param tu The TransportUnit instance to save
     * @return The updated instance
     */
    TransportUnit update(Barcode barcode, TransportUnit tu);

    /**
     * Move a {@link TransportUnit} identified by its {@link Barcode} to the
     * {@code Location} identified by the given {@code targetLocationPK}.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param targetLocationPK Unique identifier of the target Location
     * @return The moved instance
     */
    TransportUnit moveTransportUnit(Barcode barcode, LocationPK targetLocationPK);

    /**
     * Change the target of the {@link TransportUnit} identified with its {@code barcode}
     * to the Location identified by the {@code targetLocationId}.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param targetLocationId The LocationPK or {@literal null} to reset the current
     *                         target
     */
    TransportUnit changeTarget(Barcode barcode, String targetLocationId);

    /**
     * Delete already persisted {@link TransportUnit}s from the persistent storage. It is
     * not allowed in every case to delete a {@link TransportUnit}, potentially an active
     * {@code TransportOrder} could exist or Inventory is still linked with one of the
     * {@code transportUnit}s.
     *
     * @param transportUnits A collection of {@link TransportUnit}s to delete
     */
    void deleteTransportUnits(List<TransportUnit> transportUnits);

    /**
     * Find and return a {@link TransportUnit} with a particular {@link Barcode}.
     *
     * @param barcode The business identifier of the TransportUnit
     * @return The TransportUnit
     * @throws org.ameba.exception.NotFoundException may throw if not found
     */
    TransportUnit findByBarcode(Barcode barcode);

    /**
     * Find and return all {@link TransportUnit}s identified by their particular
     * {@link Barcode}.
     *
     * @param barcodes A list of business identifiers of the TransportUnits
     * @return A List of TransportUnits or an empty List, never {@literal null}
     */
    List<TransportUnit> findByBarcodes(List<Barcode> barcodes);

    /**
     * Find and return all {@link TransportUnit}s that are located on the {@code Location}
     * identified by the given {@code actualLocation}.
     *
     * @param actualLocation The Location where the TransportUnits are placed on
     * @return All TransportUnits or an empty List, never {@literal null}
     */
    List<TransportUnit> findOnLocation(@NotEmpty String actualLocation);

    /**
     * Find and return a {@link TransportUnit} identified by the given {@code pKey}.
     *
     * @param pKey The persistent key
     * @return The instance, never {@literal null}
     * @throws org.ameba.exception.NotFoundException may throw if not found
     */
    TransportUnit findByPKey(String pKey);
}
