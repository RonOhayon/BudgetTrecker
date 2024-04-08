package com.example.budgettracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions : List<Transaction>
    private lateinit var oldTransactions : List<Transaction>
    private lateinit var transactionAdapter : TransactionAdapter
    private lateinit var linearlayoutManager : LinearLayoutManager
    private lateinit var recyclerView : RecyclerView
    private lateinit var budget : TextView
    private lateinit var expanse : TextView
    private lateinit var balace : TextView
    private lateinit var addBtn : FloatingActionButton
    private lateinit var db : AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactions = arrayListOf()

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()

        findView()
        initView()
        setRecyclerView()

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
               deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }
        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerView)

    }

    private fun updateDashboard(){
        val totalAmount = transactions.map { it.amount }.sum()
        val budgetAmount = transactions.filter { it.amount >0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        balace.text = "$ %.2f".format(totalAmount)
        budget.text = "$ %.2f".format(budgetAmount)
        expanse.text = "$ %.2f".format(expenseAmount)
    }
    private fun findView(){
        recyclerView = findViewById(R.id.recyclerview)
        budget = findViewById(R.id.budget)
        expanse = findViewById(R.id.expanse)
        balace = findViewById(R.id.balance)
        addBtn = findViewById(R.id.main_fab)
    }
    private  fun initView(){
        addBtn.setOnClickListener(){
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setRecyclerView(){


        transactionAdapter = TransactionAdapter(transactions)
        linearlayoutManager = LinearLayoutManager(this)

        recyclerView.apply {
            adapter =transactionAdapter
            layoutManager = linearlayoutManager

        }
    }
    private fun fetchAll(){
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread(){
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        fetchAll()
    }
    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread{
                updateDashboard()
                transactionAdapter.setData(transactions)
                showsSnackBar()
            }
        }
    }
    private fun showsSnackBar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackBar = Snackbar.make(view,"Transaction Deleted!",Snackbar.LENGTH_LONG)
        snackBar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this , R.color.red))
            .setTextColor(ContextCompat.getColor(this , R.color.white))
            .show()
    }
    private fun undoDelete() {
        GlobalScope.launch(){
            db.transactionDao().InsertAll(deletedTransaction)
            transactions = oldTransactions

            runOnUiThread{
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }
}