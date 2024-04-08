package com.example.budgettracker

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.budgettracker.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var addBTNTransaction : Button
    private lateinit var labelEditText : TextInputEditText
    private  lateinit var labelLayout :  TextInputLayout
    private lateinit var amountEditText : TextInputEditText
    private lateinit var amountLayout: TextInputLayout
    private lateinit var description : TextInputEditText
    private lateinit var closeBtn : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        findView()
        initView()



    }
    private fun findView(){

        addBTNTransaction = findViewById(R.id.add_BTN_transaction)
        closeBtn = findViewById(R.id.add_closeBtn)

        labelEditText  = findViewById(R.id.add_labelInput)
        amountEditText  = findViewById(R.id.add_amountInput)
        labelLayout = findViewById(R.id.add_labelLayout)
        amountLayout = findViewById(R.id.add_amountLayout)
        description = findViewById(R.id.add_descriptionInput)

    }
    private fun initView(){
        labelEditText.addTextChangedListener {
            if(it!!.isNotEmpty())
                labelLayout.error = null

        }
        amountEditText.addTextChangedListener {
            if (it!!.isNotEmpty())
                amountLayout.error = null
        }


        addBTNTransaction.setOnClickListener {
            val label : String = labelEditText.text.toString()
            val amount : Double? = amountEditText.text.toString().toDoubleOrNull()
            val description : String = description.text.toString()

            if (label.isEmpty())
                labelLayout.error = "please enter valid label"

           else if(amount == null)
                amountLayout.error = "please enter valid amount"
            else{
                val transaction = Transaction(0,label,amount,description)
                insert(transaction)
            }
        }

        closeBtn.setOnClickListener(){
            finish()
        }
    }
    private fun insert(transaction: Transaction){
        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().InsertAll(transaction)
            finish()
        }
    }
}