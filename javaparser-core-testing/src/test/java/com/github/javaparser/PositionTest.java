package com.github.javaparser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;

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
}
