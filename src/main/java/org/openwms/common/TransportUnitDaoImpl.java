/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.common;

import org.openwms.core.integration.jpa.AbstractGenericJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A TransportUnitDaoImpl.
 * 
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision$
 * @since 0.1
 * @see org.openwms.core.integration.jpa.AbstractGenericJpaDao
 * @see TransportUnitDao
 */
@Transactional(propagation = Propagation.MANDATORY)
@Repository(TransportUnitDaoImpl.COMPONENT_NAME)
public class TransportUnitDaoImpl extends AbstractGenericJpaDao<TransportUnit, Long> implements TransportUnitDao {

    /** Springs component name. */
    public static final String COMPONENT_NAME = "transportUnitDao";

    /**
     * {@inheritDoc}
     * 
     * @see org.openwms.core.integration.jpa.AbstractGenericJpaDao#getFindAllQuery()
     */
    @Override
    protected String getFindAllQuery() {
        return TransportUnit.NQ_FIND_ALL;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.openwms.core.integration.jpa.AbstractGenericJpaDao#getFindByUniqueIdQuery()
     */
    @Override
    protected String getFindByUniqueIdQuery() {
        return TransportUnit.NQ_FIND_BY_UNIQUE_QUERY;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.openwms.core.integration.jpa.AbstractGenericJpaDao#getPersistentClass()
     */
    @Override
    protected Class<TransportUnit> getPersistentClass() {
        return TransportUnit.class;
    }
}