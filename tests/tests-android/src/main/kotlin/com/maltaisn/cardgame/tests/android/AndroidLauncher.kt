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

package com.maltaisn.cardgame.tests.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maltaisn.cardgame.tests.core.CardGameTests

class AndroidLauncher : AppCompatActivity() {

    private lateinit var runBtn: Button
    private lateinit var testRcv: RecyclerView


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_chooser)

        val prefs = getSharedPreferences("testPrefs", Context.MODE_PRIVATE)

        testRcv = findViewById(R.id.rcv_tests)
        val keys = CardGameTests.TESTS_MAP.keys.toList()
        val adapter = TestAdapter(keys.map { TestItem(it, false) })
        testRcv.layoutManager = LinearLayoutManager(this)
        testRcv.adapter = adapter

        runBtn = findViewById(R.id.btn_run)
        runBtn.setOnClickListener {
            val testName = adapter.selectedItem!!.name
            prefs.edit().putString("lastTest", testName).apply()
            runTest(testName)
        }

        // Select last ran test
        val lastTestIndex = keys.indexOf(prefs.getString("lastTest", null) ?: "")
        if (lastTestIndex != -1) {
            adapter.selectAt(lastTestIndex)
            testRcv.scrollToPosition(lastTestIndex)
            runBtn.isEnabled = true
        }
    }

    private fun runTest(testName: String) {
        val intent = Intent(this, GameTestActivity::class.java)
        intent.putExtra(GameTestActivity.EXTRA_TEST_NAME, testName)
        startActivity(intent)
    }

    private inner class TestAdapter(val items: List<TestItem>) :
            RecyclerView.Adapter<TestViewHolder>() {

        var selectedIndex = RecyclerView.NO_POSITION

        val selectedItem: TestItem?
            get() = items.getOrNull(selectedIndex)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                TestViewHolder(LayoutInflater.from(this@AndroidLauncher)
                        .inflate(R.layout.item_test, parent, false))

        override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
            holder.bind(items[position])
            holder.radioBtn.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectAt(position)
                }
            }
        }

        override fun getItemCount() = items.size

        fun selectAt(index: Int) {
            if (index !in items.indices) return

            // Uncheck last item
            if (selectedIndex != RecyclerView.NO_POSITION) {
                items[selectedIndex].checked = false
                notifyItemChanged(selectedIndex)
            }

            // Check new item
            selectedIndex = index
            items[selectedIndex].checked = true

            runBtn.isEnabled = true
        }
    }

    private class TestViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val radioBtn: RadioButton = view.findViewById(R.id.radio_test)

        fun bind(item: TestItem) {
            radioBtn.text = item.name
            radioBtn.isChecked = item.checked
        }
    }

    private data class TestItem(val name: String, var checked: Boolean)

}
