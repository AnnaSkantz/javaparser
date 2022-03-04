package com.github.javaparser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PositionTest {

    @Test
    void testWithLineWhenPositionIsCorrect()  {
        Position actualPosition = new Position(3, 5);
        Position expectedPosition = new Position(6, 5);
        actualPosition = actualPosition.withLine(6);
        assertEquals(expectedPosition, actualPosition);
    }

    @Test
    void testWithLineWhenPositionIsIncorrect()  {
        Position actualPosition = new Position(3, 5);
        Position expectedPosition = new Position(6, 5);
        actualPosition = actualPosition.withLine(8);
        assertNotEquals(expectedPosition, actualPosition);
    }

    @Test
    void testOrIsInvalidReturnsThisPositionWhenItsValid()  {
        Position thisPosition = new Position(3, 5);
        Position otherPosition = new Position(6, 5);
        Position actualPosition = thisPosition.orIfInvalid(otherPosition);
        assertEquals(thisPosition, actualPosition);
    }

    @Test
    void testOrIsInvalidReturnsOtherPositionWhenItsValid()  {
        Position thisPosition = new Position(0, 5);
        Position otherPosition = new Position(6, 5);
        Position actualPosition = thisPosition.orIfInvalid(otherPosition);
        assertEquals(otherPosition, actualPosition);
    }

    @Test
    void testOrIsInvalidReturnsOtherPositionWhenBothAreInvalid()  {
        Position thisPosition = new Position(0, 5);
        Position otherPosition = new Position(0, 5);
        Position actualPosition = thisPosition.orIfInvalid(otherPosition);
        assertEquals(otherPosition, actualPosition);
    }
}
