/*
 * Copyright (C) 2019 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.csstudio.apputil.formula.array;

import org.epics.util.array.ArrayDouble;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDouble;
import org.epics.vtype.VInt;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ElementAtNumberFunctionTest {

    @Test
    public void computeNumber() throws Exception{
        ElementAtNumberFunction elementAtNumberFunction =
                new ElementAtNumberFunction();

        VType index = VDouble.of(2.0, Alarm.none(), Time.now(), Display.none());
        VType array = VNumberArray.of(ArrayDouble.of(1.0, 2.0, 3.0), Alarm.none(), Time.now(), Display.none());

        VDouble vDouble = (VDouble) elementAtNumberFunction.compute(array, index);
        assertEquals(3, vDouble.getValue().intValue());

        index = VInt.of(2, Alarm.none(), Time.now(), Display.none());
        vDouble = (VDouble) elementAtNumberFunction.compute(array, index);
        assertEquals(3, vDouble.getValue().intValue());

        vDouble = (VDouble) elementAtNumberFunction.compute(index, index);

        assertTrue(vDouble.getValue().isNaN());
    }

    @Test
    public void computeString() throws Exception{
        ElementAtNumberFunction elementAtNumberFunction =
                new ElementAtNumberFunction();

        VType index = VDouble.of(2.0, Alarm.none(), Time.now(), Display.none());
        VType array = VStringArray.of(List.of("a", "b", "c"));
        VString vString = (VString) elementAtNumberFunction.compute(array, index);

        assertEquals("c", vString.getValue());

        vString = (VString)elementAtNumberFunction.compute(array, array);

        assertEquals("", vString.getValue());
    }

    @Test(expected = Exception.class)
    public void invalidArguments1() throws Exception{
        ElementAtNumberFunction elementAtNumberFunction =
                new ElementAtNumberFunction();

        VType index = VDouble.of(8.0, Alarm.none(), Time.now(), Display.none());
        VType array = VNumberArray.of(ArrayDouble.of(1.0, 2.0, 3.0), Alarm.none(), Time.now(), Display.none());

        elementAtNumberFunction.compute(array, index);
    }

    @Test(expected = Exception.class)
    public void invalidArguments2() throws Exception{
        ElementAtNumberFunction elementAtNumberFunction =
                new ElementAtNumberFunction();

        VType index = VDouble.of(-1.0, Alarm.none(), Time.now(), Display.none());
        VType array = VNumberArray.of(ArrayDouble.of(1.0, 2.0, 3.0), Alarm.none(), Time.now(), Display.none());

        elementAtNumberFunction.compute(array, index);
    }
}