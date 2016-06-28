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
package org.openwms.common.transport;

import static org.junit.Assert.fail;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openwms.common.location.Location;
import org.openwms.common.location.LocationPK;
import org.openwms.core.test.AbstractJpaSpringContextTests;
import org.springframework.test.context.ContextConfiguration;

/**
 * A TransportUnitTest.
 * 
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision$
 * @since 0.1
 */
@ContextConfiguration("classpath:common-jpa-test-context.xml")
public class TransportUnitTest extends AbstractJpaSpringContextTests {

    /**
     * Try to persist TransportUnit without TransportUnitType.
     */
    @Test
    public final void testTUwithoutType() {
        TransportUnit transportUnit = new TransportUnit("NEVER_PERSISTED");
        try {
            entityManager.persist(transportUnit);
            entityManager.flush();
            fail("Persisting without TransportUnitType not allowed!");
        } catch (PersistenceException pe) {
            // okay
            LOGGER.debug("OK:Execption while persisting TransportUnit without TransportUnitType.");
        }
    }

    /**
     * Try to instantiate TransportUnit with unknown TransportUnitType.
     */
    @Test
    public final void testTUwithUnknownType() {
        TransportUnit transportUnit = new TransportUnit("NEVER_PERSISTED");
        TransportUnitType transportUnitType = new TransportUnitType("UNKNOWN_TUT");
        transportUnit.setTransportUnitType(transportUnitType);
        try {
            entityManager.persist(transportUnit);
            fail("Persisting with unknown TransportUnitType not allowed!");
        } catch (Exception pe) {
            // okay
            LOGGER.debug("OK:Exception while persisting TransportUnit with unknown TransportUnitType.");
        }
    }

    /**
     * Try to persist TransportUnit with an unknown actualLocation.
     */
    @Test
    public final void testTUwithUnknownLocations() {
        TransportUnit transportUnit = new TransportUnit("NEVER_PERSISTED");
        TransportUnitType transportUnitType = new TransportUnitType("WELL_KNOWN_TUT");
        Location actualLocation = new Location(new LocationPK("UNKN", "UNKN", "UNKN", "UNKN", "UNKN"));

        entityManager.persist(transportUnitType);

        transportUnit.setTransportUnitType(transportUnitType);
        transportUnit.setActualLocation(actualLocation);
        try {
            entityManager.persist(transportUnit);
            fail("Persisting with unknown actualLocation && targetLocation not allowed!");
        } catch (Exception pe) {
            // okay
            LOGGER.debug("OK:Execption while persisting TransportUnit with unknown actualLocation and targetLocation.");
        }
    }

    /**
     * Try to persist TransportUnit with known TransportUnitType and a known actualLocation.
     */
    @Test
    public final void testTUwithKnownLocation() {
        TransportUnit transportUnit = new TransportUnit("TEST_TU");
        TransportUnitType transportUnitType = new TransportUnitType("WELL_KNOWN_TUT_2");
        Location location = new Location(new LocationPK("KNO4", "KNO4", "KNO4", "KNO4", "KNO4"));

        entityManager.persist(transportUnitType);
        entityManager.persist(location);

        transportUnit.setTransportUnitType(transportUnitType);
        transportUnit.setActualLocation(location);

        try {
            entityManager.merge(transportUnit);
            LOGGER.debug("Also without targetLocation must be okay.");
        } catch (PersistenceException pe) {
            LOGGER.debug("NOT OK:Execption while persisting TransportUnit with known actualLocation and transportUnitType!");
            fail("Persisting transportUnit with known actualLocation and transportUnitType not committed!");
        }
    }

    /**
     * Test cascading UnitErrors with TransportUnits.
     */
    @Ignore
    @Test
    public final void testTUwithErrors() {
        TransportUnit transportUnit = new TransportUnit(new Barcode("TEST_TU3"));
        TransportUnitType transportUnitType = new TransportUnitType("WELL_KNOWN_TUT_4");
        Location location = new Location(new LocationPK("KNOWN3", "KNOWN3", "KNOWN3", "KNOWN3", "KNOWN3"));

        entityManager.persist(transportUnitType);
        entityManager.persist(location);

        transportUnit.setTransportUnitType(transportUnitType);
        transportUnit.setActualLocation(location);
        transportUnit.setTargetLocation(location);

        transportUnit.addError(new UnitError());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            LOGGER.error("Error", e);
        }
        transportUnit.addError(new UnitError());
        try {
            entityManager.persist(transportUnit);
        } catch (Exception pe) {
            fail("Persisting with well known Location and TransportUnitType fails!");
        }

        Query query = entityManager.createQuery("select count(ue) from UnitError ue");
        Long cnt = (Long) query.getSingleResult();
        Assert.assertEquals("Expected 2 persisted UnitErrors", 2, cnt.intValue());

        entityManager.remove(transportUnit);

        cnt = (Long) query.getSingleResult();
        Assert.assertEquals("Expected 0 persisted UnitErrors", 0, cnt.intValue());
    }

    /**
     * Try to persist a TransportUnit with well known actualLocation and a well known TransportUnitType. The targetLocation is unknown.
     */
    @Test
    public final void testTUwithKnownLocations() {
        TransportUnit transportUnit = new TransportUnit("TEST_TU2");
        TransportUnitType transportUnitType = new TransportUnitType("WELL_KNOWN_TUT_3");
        Location actualLocation = new Location(new LocationPK("KNO2", "KNO2", "KNO2", "KNO2", "KNO2"));
        Location targetLocation = new Location(new LocationPK("UNKN", "UNKN", "UNKN", "UNKN", "UNKN"));

        entityManager.persist(transportUnitType);
        entityManager.persist(actualLocation);

        transportUnit.setTransportUnitType(transportUnitType);
        transportUnit.setActualLocation(actualLocation);
        transportUnit.setTargetLocation(targetLocation);
        try {
            entityManager.persist(transportUnit);
            // FIXME [scherrer] :
            // fail("Persisting with unknown targetLocation must fail!");
        } catch (Exception pe) {
            // okay
            LOGGER.debug("OK:Execption while persisting TransportUnit with unknown targetLocation.");
        }
    }
}