package com.shinetech.tollview

import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.models.User
import org.junit.Assert.*
import org.junit.Test
import java.sql.Timestamp

class UserTest {

    @Test
    fun testUserInstantiationWithTolls() {
        val timestamp1 = Timestamp(System.currentTimeMillis())
        val toll1 = Toll(gateId = "1", timestamp = timestamp1)

        val timestamp2 = Timestamp(System.currentTimeMillis() + 1000)
        val toll2 = Toll(gateId = "2", timestamp = timestamp2)

        val tollsList = arrayListOf(toll1, toll2)
        val user = User(tolls = tollsList)

        assertEquals(2, user.tolls.size)
        assertEquals(toll1, user.tolls[0])
        assertEquals(toll2, user.tolls[1])
    }

    @Test
    fun testUserInstantiationWithEmptyTolls() {
        val tollsList = arrayListOf<Toll>()
        val user = User(tolls = tollsList)

        assertTrue(user.tolls.isEmpty())
    }

    @Test
    fun testAddTollToUser() {
        val tollsList = arrayListOf<Toll>()
        val user = User(tolls = tollsList)

        val timestamp = Timestamp(System.currentTimeMillis())
        val newToll = Toll(gateId = "3", timestamp = timestamp)

        user.tolls.add(newToll)

        assertEquals(1, user.tolls.size)
        assertEquals(newToll, user.tolls[0])
    }

    @Test
    fun testRemoveTollFromUser() {
        val timestamp = Timestamp(System.currentTimeMillis())
        val tollToRemove = Toll(gateId = "3", timestamp = timestamp)
        val tollsList = arrayListOf(tollToRemove)
        val user = User(tolls = tollsList)

        user.tolls.remove(tollToRemove)

        assertTrue(user.tolls.isEmpty())
    }
}
