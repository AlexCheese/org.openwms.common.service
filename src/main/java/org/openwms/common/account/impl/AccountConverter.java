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
package org.openwms.common.account.impl;

import org.ameba.exception.NotFoundException;
import org.ameba.i18n.Translator;
import com.github.dozermapper.core.DozerConverter;
import org.openwms.common.CommonMessageCodes;
import org.openwms.common.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import static org.openwms.common.CommonMessageCodes.ACCOUNT_NOT_FOUND;

/**
 * A AccountConverter.
 *
 * @author Heiko Scherrer
 */
@Configurable
public class AccountConverter extends DozerConverter<String, Account> {

    @Lazy
    @Autowired
    private AccountRepository repository;
    @Lazy
    @Autowired
    private Translator translator;

    public AccountConverter() {
        super(String.class, Account.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account convertTo(String source, Account destination) {
        if (source == null) {
            return null;
        }
        return repository.findByIdentifier(source)
                .orElseGet(() -> repository.findByName(source)
                        .orElseThrow(() -> new NotFoundException(translator, ACCOUNT_NOT_FOUND,
                                new String[]{source}, source))
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertFrom(Account source, String destination) {
        if (source == null) {
            return null;
        }
        return source.getIdentifier();
    }
}
