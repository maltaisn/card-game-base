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


/**
 * O(n^4) Implementation of the Munkres algorithm (or Hungarian) for solving the assignment problem.
 *
 * References:
 * - https://en.wikipedia.org/wiki/Hungarian_algorithm#Matrix_interpretation
 * - http://csclab.murraystate.edu/~bob.pilgrim/445/munkres.html
 * - https://github.com/bmc/munkres/blob/master/munkres.py
 *
 * @property costMatrix The cost matrix. The matrix is changed so a copy must be made
 * to keep its original content. Use [DISALLOWED] for disallowed assignments.
 */
class Munkres(private val costMatrix: Array<IntArray>) {

    private val n = costMatrix.size

    // BooleanArray is about 4 times faster than BitSet, although less memory efficient.
    private val rowCover = BooleanArray(n)
    private val colCover = BooleanArray(n)

    private val maskMatrix = Array(n) { IntArray(n) { MASK_NONE } }

    private val path = IntArray(n * 4)
    private var pathSize = 0
    private var zeroRow = -1
    private var zeroCol = -1

    private val assignment: IntArray
        get() {
            // Assignment is given by the position of the starred zeroes in the matrix.
            val assignment = IntArray(n) { -1 }
            for (row in 0 until n) {
                for (col in 0 until n) {
                    if (maskMatrix[row][col] == MASK_STAR) {
                        assignment[row] = col
                        break
                    }
                }
            }
            assert(assignment.none { it == -1 })  // All tasks must be assigned.
            return assignment
        }


    /**
     * Find the optimal assignment for the [costMatrix].
     * The assignment is given as the column index for each row.
     */
    fun findOptimalAssignment(): IntArray {
        if (costMatrix.isEmpty()) return IntArray(0)
        if (costMatrix.size == 1) return intArrayOf(0)

        reduceMatrix()  // Step 1
        starZeroes()  // Step 2

        while (true) {
            // Step 3
            if (coverColsWithStarredZero() == n) {
                // Step 7
                return assignment
            }

            while (true) {
                // Step 4
                primeUncoveredZero()
                if (zeroRow == -1) {
                    // Step 6
                    createZeroes()

                } else {
                    val col = maskMatrix[zeroRow].indexOfFirst { it == MASK_STAR }
                    if (col == -1) {
                        // Step 5
                        // No starred zero found in row.
                        findAugmentingPath()
                        break
                    } else {
                        // Cover the row and uncover the column of the starred zero found.
                        rowCover[zeroRow] = true
                        colCover[col] = false
                    }
                }
            }
        }
    }

    /**
     * Reduce the matrix to create initial zeroes.
     */
    private fun reduceMatrix() {
        for (row in 0 until n) {
            // Subtract the minimum element of the row to every element of it.
            val min = costMatrix[row].min()!!
            for (col in 0 until n) {
                costMatrix[row][col] -= min
            }
        }
    }

    /**
     * Greedily star zeroes, making sure there is at most one starred zero per row and column.
     * Returns the number of columns with a starred zero.
     */
    private fun starZeroes() {
        for (row in 0 until n) {
            for (col in 0 until n) {
                if (!colCover[col] && !rowCover[row] && costMatrix[row][col] == 0) {
                    rowCover[row] = true
                    colCover[col] = true
                    maskMatrix[row][col] = MASK_STAR
                }
            }
        }

        // Clear all line covers
        colCover.clear()
        rowCover.clear()
    }

    /**
     * Cover columns with starred zeroes in it.
     */
    private fun coverColsWithStarredZero(): Int {
        var count = 0
        for (col in 0 until n) {
            for (row in 0 until n) {
                if (maskMatrix[row][col] == MASK_STAR) {
                    colCover[col] = true
                    count++
                    break
                }
            }
        }
        return count
    }

    /**
     * Prime an uncovered zero, returning the row in which the zero is,
     * or `-1` if no uncovered zero was found.
     */
    private fun primeUncoveredZero() {
        for (row in 0 until n) {
            if (!rowCover[row]) {
                for (col in 0 until n) {
                    if (!colCover[col] && costMatrix[row][col] == 0) {
                        // Found an uncovered zero, prime it.
                        maskMatrix[row][col] = MASK_PRIME
                        zeroRow = row
                        zeroCol = col
                        return
                    }
                }
            }
        }
        zeroRow = -1
        zeroCol = -1
    }

    /**
     * Starting with a [start] primed zero element, create a list of alternating
     * primed and starred zeroes. Then unstar all starred and star all primed
     * elements of the list.
     */
    private fun findAugmentingPath() {
        pathSize = 0
        addPathElement()

        while (true) {
            // Find a starred zero in the column of the last zero.
            zeroRow = maskMatrix.indexOfFirst { it[zeroCol] == MASK_STAR }
            if (zeroRow == -1) break
            addPathElement()

            // Find a primed zero in the row of the last zero.
            zeroCol = maskMatrix[zeroRow].indexOfFirst { it == MASK_PRIME }
            addPathElement()
        }

        // Unstar all starred and star all primed.
        for (i in 0 until pathSize - 1) {
            val row = path[i]
            val col = path[i + 1]
            maskMatrix[row][col] = if (maskMatrix[row][col] == MASK_STAR) {
                MASK_NONE
            } else {
                MASK_STAR
            }
        }

        // Erase all primes
        for (row in 0 until n) {
            for (col in 0 until n) {
                if (maskMatrix[row][col] == MASK_PRIME) {
                    maskMatrix[row][col] = MASK_NONE
                }
            }
        }

        // Clear all line covers
        rowCover.clear()
        colCover.clear()
    }

    private fun addPathElement() {
        path[pathSize++] = zeroRow
        path[pathSize++] = zeroCol
    }

    /**
     * Find the lowest uncovered value and subtract in to every uncovered
     * element and add it to every elements covered twice, creating new zeroes.
     */
    private fun createZeroes() {
        // Find lowest value in uncovered values
        var min = Int.MAX_VALUE
        for (row in 0 until n) {
            if (!rowCover[row]) {
                for (col in 0 until n) {
                    if (!colCover[col] && costMatrix[row][col] < min) {
                        min = costMatrix[row][col]
                    }
                }
            }
        }
        assert(min > 0)  // If minimum is zero, the algorithm will never end.

        // Subtract min from uncovered elements and add it to elements covered twice
        for (row in 0 until n) {
            for (col in 0 until n) {
                if (!rowCover[row] && !colCover[col]) {
                    costMatrix[row][col] -= min
                } else if (rowCover[row] && colCover[col]) {
                    costMatrix[row][col] += min
                }
            }
        }
    }

    private fun BooleanArray.clear() {
        for (i in 0 until size) {
            this[i] = false
        }
    }

    companion object {
        const val DISALLOWED = Int.MAX_VALUE

        private const val MASK_NONE = 0
        private const val MASK_STAR = 1
        private const val MASK_PRIME = 2
    }

}
