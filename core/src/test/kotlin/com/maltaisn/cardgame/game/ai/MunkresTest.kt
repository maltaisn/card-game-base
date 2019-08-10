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

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat
import kotlin.random.Random
import kotlin.system.measureNanoTime


internal class MunkresTest {

    @Test
    fun `Simple matrix 1, n=3`() = validateAssignment(arrayOf(
            intArrayOf(250, 400, 350),
            intArrayOf(400, 600, 350),
            intArrayOf(200, 400, 250)))

    @Test
    fun `Simple matrix 2, n=3`() = validateAssignment(arrayOf(
            intArrayOf(1, 2, 3),
            intArrayOf(2, 4, 6),
            intArrayOf(3, 6, 9)))

    @Test
    fun `Random matrices, n=3`() {
        repeat(100000) {
            validateAssignment(randomMatrix(3))
            println(it + 1)
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
                Munkres(worstCaseMatrix(size)).findOptimalAssignment()
            }
            println("n=$size, t=${NumberFormat.getInstance().format(time / 1E6)} ms")
        }
    }

    @Test
    fun `Performance test, n=1024`() {
        Munkres(worstCaseMatrix(1024)).findOptimalAssignment()
    }


    private fun validateAssignment(costMatrix: Array<IntArray>) {
        val matrixCopy = Array(costMatrix.size) { costMatrix[it].clone() }

        val assignment = Munkres(costMatrix).findOptimalAssignment()

        // All rows must be matched to an unique column.
        assertEquals("Wrong assignment for matrix: ${matrixCopy.contentDeepToString()}",
                costMatrix.size, assignment.toSet().size)

        // All assignments must be zeroes in cost matrix.
        for ((row, col) in assignment.withIndex()) {
            assertEquals(0, costMatrix[row][col])
        }
    }

    private fun randomMatrix(n: Int) = Array(n) {
        IntArray(n) {
            Random.nextInt(1024)
        }
    }

    private fun worstCaseMatrix(n: Int) = Array(n) { row ->
        IntArray(n) { col ->
            (row + 1) * (col + 1)
        }
    }

}
