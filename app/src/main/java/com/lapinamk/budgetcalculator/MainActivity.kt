package com.lapinamk.budgetcalculator

import java.math.RoundingMode
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var etName: TextView
    private lateinit var etAmount: TextView
    private lateinit var switchIncomeExpense: Switch
    private lateinit var btnAdd: Button
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var btnClearAll: Button
    private lateinit var recyclerView: RecyclerView
    private var budgetItems = mutableListOf<BudgetItem>()
    private lateinit var budgetItemAdapter: BudgetItemAdapter
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        etAmount = findViewById(R.id.etAmount)
        switchIncomeExpense = findViewById(R.id.switchIncomeExpense)
        btnAdd = findViewById(R.id.btnAdd)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvBalance = findViewById(R.id.tvBalance)
        btnClearAll = findViewById(R.id.btnClearAll)
        recyclerView = findViewById(R.id.recyclerView)
        budgetItemAdapter = BudgetItemAdapter(budgetItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = budgetItemAdapter
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        loadBudgetItems()
        updateTotals()

        switchIncomeExpense.setOnClickListener {
            if (switchIncomeExpense.isChecked) {
                switchIncomeExpense.text = "Income selected"
                switchIncomeExpense.setTextColor(Color.parseColor("#FF669900"))
            } else {
                switchIncomeExpense.text = "Expense selected"
                switchIncomeExpense.setTextColor(Color.parseColor("#FF4444"))
            }
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val amountString = etAmount.text.toString().trim()
            if (name.isEmpty() || amountString.isEmpty()) {
                return@setOnClickListener
            }
            val amount = amountString.toDouble()
            val isIncome = switchIncomeExpense.isChecked
            budgetItems.add(BudgetItem(name, amount, isIncome))
            it.hideKeyboard()
            budgetItemAdapter.notifyDataSetChanged()
            updateTotals()
            etName.text = ""
            etAmount.text = ""
        }

        btnClearAll.setOnClickListener {
            budgetItems.clear()
            budgetItemAdapter.notifyDataSetChanged()
            updateTotals()
        }
    }

    private fun saveBudgetItems(budgetItems: MutableList<BudgetItem>) {
        val gson = Gson()
        val json = gson.toJson(budgetItems)
        sharedPref.edit().putString("budget_items", json).apply()
    }

    private fun loadBudgetItems() {
        val gson = Gson()
        val json = sharedPref.getString("budget_items", "")
        val type = object : TypeToken<MutableList<BudgetItem>>() {}.type

        if (json!!.isNotEmpty()) {
            budgetItems = gson.fromJson(json, type)
            budgetItemAdapter = BudgetItemAdapter(budgetItems)
            recyclerView.adapter = budgetItemAdapter
        }
    }

    private fun updateTotals() {
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (item in budgetItems) {
            if (item.isIncome) {
                totalIncome += item.amount
            } else {
                totalExpense += item.amount
            }
        }
        tvTotalIncome.text = "▲ ${totalIncome.toString().toFloat().toBigDecimal().setScale(2, RoundingMode.HALF_UP)}"
        tvTotalExpense.text = "▼ ${totalExpense.toString().toFloat().toBigDecimal().setScale(2, RoundingMode.HALF_UP)}"
        tvBalance.text = "Σ ${(totalIncome - totalExpense).toString().toFloat().toBigDecimal().setScale(2, RoundingMode.HALF_UP)}"
        saveBudgetItems(budgetItems)
    }

    private fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    inner class BudgetItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvName: TextView
        private var tvAmount: TextView
        private var btnDelete: Button

        init {
            tvName = itemView.findViewById(R.id.tvName)
            tvAmount = itemView.findViewById(R.id.tvAmount)
            btnDelete = itemView.findViewById(R.id.btnDelete)
        }

        fun bind(budgetItem: BudgetItem) {
            tvName.text = budgetItem.name
            tvAmount.text = "${(budgetItem.amount).toString().toFloat().toBigDecimal().setScale(2, RoundingMode.HALF_UP)}"
            tvAmount.setTextColor(Color.parseColor(if (budgetItem.isIncome) "#FF669900" else "#FF4444"))
            btnDelete.setOnClickListener {
                budgetItems.remove(budgetItem)
                budgetItemAdapter.notifyDataSetChanged()
                updateTotals()
            }
        }
    }

    inner class BudgetItemAdapter(private val budgetItems: List<BudgetItem>) :
        RecyclerView.Adapter<BudgetItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_budget, parent, false)
            return BudgetItemViewHolder(view)
        }

        override fun getItemCount() = budgetItems.size

        override fun onBindViewHolder(holder: BudgetItemViewHolder, position: Int) {
            holder.bind(budgetItems[position])
        }
    }

    data class BudgetItem(val name: String, val amount: Double, val isIncome: Boolean)
}