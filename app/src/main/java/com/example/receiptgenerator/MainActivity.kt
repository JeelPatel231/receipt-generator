package com.example.receiptgenerator

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import java.util.concurrent.atomic.AtomicInteger
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.receiptgenerator.databinding.ActivityMainBinding
import kotlin.math.round
import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder
import java.util.*


// INITIALIZE PUBLIC VARS
val publicMap = mutableMapOf<String,Int>()
lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.autocomplete.setAdapter(
            ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, productArray.keys.toList())
        )

        binding.add.setOnClickListener{
            if(binding.autocomplete.text.toString() != ""){
                increment(publicMap,binding.autocomplete.text.toString())
                updateRecycler()
            }
        }

        binding.generate.setOnClickListener{
//            Toast.makeText(this,generate().toString(),Toast.LENGTH_SHORT).show()
//            Toast.makeText(this, totalMoney().toString(),Toast.LENGTH_SHORT).show()
            buildReceipt()
        }

        binding.clear.setOnClickListener{
            publicMap.clear()
            updateRecycler()
        }

        binding.imageView.setOnClickListener{
            binding.imageView.visibility = View.GONE
        }

    }

}

// FUNCTIONS TO ADD TO LIST
fun <K> increment(map: MutableMap<K, Int>, key: K) {
    val value = if (map.containsKey(key)) map[key] else 0
    map[key] = AtomicInteger(value!!).incrementAndGet()
}

fun <K> decrement(map: MutableMap<K, Int>, key: K) {
    val value = map[key]
    map[key] = AtomicInteger(value!!).decrementAndGet()
    if(map[key] == 0){map.remove(key)}
}

// RECYCLER VIEW
fun updateRecycler(){
    binding.recyclerView.adapter = CustomAdapter(publicMap.toList());
}

class CustomAdapter(private val itemList: List<Pair<String,Int>>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val items = itemList[position]
        holder.itemName.text = items.first
        holder.count.text = items.second.toString()
        holder.decrement.setOnClickListener{ decrement(publicMap,items.first) ; updateRecycler()}
        holder.increment.setOnClickListener{ increment(publicMap,items.first) ; updateRecycler()}

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val count: TextView = itemView.findViewById(R.id.count)
        //buttons
        val decrement: Button = itemView.findViewById(R.id.decrement)
        val increment: Button = itemView.findViewById(R.id.increment)
    }
}

// generating the list
fun generate(): MutableMap<String, Double> {
    val responseArray = mutableMapOf<String,Double>()
    publicMap.forEach{
        responseArray["${it.value} x ${it.key}"] = round(productArray[it.key]!! * it.value * 100)/100
    }
    println(responseArray)
    return responseArray
}

fun totalMoney(): Double {
    var answer = 0.0
    generate().values.forEach{
        answer += it
    }
    return answer
}

//Receipt BUilder

fun buildReceipt(){
    val date = DateFormat.format("dd/MM/yyyy", Date()).toString()
    val totalprice = totalMoney()
    val ExclBtw: Double = totalprice * 0.91f
    val BTW: Double = totalprice * 0.09f
    val totalpricefloat: Double = totalprice
    val receipt = ReceiptBuilder(1200)
    receipt.setMargin(30, 20).setAlign(Paint.Align.CENTER)
        .setColor(Color.BLACK).setTextSize(60F)
        .addText("my store")
        .addText("Tel: 010-1234567").addBlankSpace(30).setAlign(Paint.Align.LEFT)
        .addText(binding.tableNumber.text.toString(), false).setAlign(Paint.Align.RIGHT).addText(date)
        .setAlign(Paint.Align.LEFT).addParagraph().addLine().addParagraph().addParagraph()
        .addBlankSpace(30)
        .setTextSize(50F);
        generate().forEach{
            receipt.setAlign(Paint.Align.LEFT).
            setTextSize(42F).
            addText(it.key, false).
            setAlign(Paint.Align.RIGHT).
            addText("€ " + it.value)
        }
    receipt.setAlign(Paint.Align.LEFT).addParagraph().addLine().addParagraph().
            addText("Excl.", false).
            setAlign(Paint.Align.RIGHT).
            addText("€" + String.format("%.2f", ExclBtw)).
            setAlign(Paint.Align.LEFT).
            addParagraph().
            addText("BTW", false).
            setAlign(Paint.Align.RIGHT).
            addText("€" + String.format("%.2f", BTW)).
            setAlign(Paint.Align.LEFT).
            addParagraph().
            addText("Totaal", false).
            setAlign(Paint.Align.RIGHT).
            addText("€" + String.format("%.2f", totalpricefloat)).
            setAlign(Paint.Align.LEFT).
            addLine(180).
            addParagraph().
            setAlign(Paint.Align.CENTER).addParagraph().addLine().addParagraph().addText("Tot Ziens")

    val bitmap = receipt.build() // this is the final bitmap
    binding.imageView.setImageBitmap(bitmap)
    binding.imageView.visibility = ImageView.VISIBLE
}