/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.cardgame.game.ai

import com.maltaisn.cardgame.utils.Hungarian
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.text.NumberFormat
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.assertNotEquals


internal class HungarianTest {

    @Test
    fun `Simple matrix 1, n=3`() = validateAssignment(arrayOf(
            floatArrayOf(250f, 400f, 350f),
            floatArrayOf(400f, 600f, 350f),
            floatArrayOf(200f, 400f, 250f)))

    @Test
    fun `Simple matrix 2, n=3`() = validateAssignment(arrayOf(
            floatArrayOf(1f, 2f, 3f),
            floatArrayOf(2f, 4f, 6f),
            floatArrayOf(3f, 6f, 9f)))

    @Test
    fun `Random matrices, n=3`() {
        repeat(1000) {
            validateAssignment(randomMatrix(3))
        }
    }

    @Test
    fun `Random matrices, n=2-128`() {
        for (n in 2..128) {
            validateAssignment(randomMatrix(n))
            println(n)
        }
    }

    @Test
    fun `Random matrices with disallowed, n=2-128`() {
        for (n in 2..128) {
            // Build a cost matrix with only disallowed then add an unique assignment.
            val costMatrix = Array(n) { FloatArray(n) { Hungarian.DISALLOWED } }
            val jobs = (0 until n).toMutableList()
            for (i in 0 until n) {
                val j = jobs.removeAt(jobs.indices.random())
                costMatrix[i][j] = Random.nextFloat()
            }

            validateAssignment(costMatrix)
            println(n)
        }
    }

    @Test
    fun `Worst case matrices, n=2-128`() {
        for (n in 2..128) {
            validateAssignment(worstCaseMatrix(n))
            println(n)
        }
    }

    @Test
    fun `Performance test, worst case matrices, powers of 2`() {
        for (n in 1..10) {
            val size = 1 shl n
            val time = measureNanoTime {
                Hungarian(worstCaseMatrix(size)).execute()
            }
            println("n=$size, t=${NumberFormat.getInstance().format(time / 1E6)} ms")
        }
    }

    @Test
    fun `Performance test, n=1024`() {
        Hungarian(worstCaseMatrix(1024)).execute()
    }


    private fun validateAssignment(costMatrix: Array<FloatArray>) {
        val matrixCopy = Array(costMatrix.size) { costMatrix[it].clone() }

        val assignment = Hungarian(costMatrix).execute()!!

        val message = "Wrong assignment for matrix: ${matrixCopy.contentDeepToString()}," +
                "assignment = ${assignment.contentToString()}"

        // All rows must be matched to an unique column.
        val assignmentSet = assignment.toSet()
        assertEquals(message, costMatrix.size, assignmentSet.size)
        assertFalse(message, -1 in assignmentSet)

        // Check if disallowed assignments weren't assigned.
        for ((x, y) in assignment.withIndex()) {
            assertNotEquals(Hungarian.DISALLOWED, costMatrix[x][y])
        }
    }

    private fun randomMatrix(n: Int) = Array(n) {
        FloatArray(n) { Random.nextFloat() }
    }

    private fun worstCaseMatrix(n: Int) = Array(n) { row ->
        FloatArray(n) { col -> (row + 1).toFloat() * (col + 1) }
    }

}
