package hu.bme.aut.android.portfoliotracker.fragments

import android.app.AlertDialog
import android.app.SearchManager
import android.content.ContentValues.TAG
import android.content.res.TypedArray
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.dift.ui.SwipeToAction
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.User
import hu.bme.aut.android.portfoliotracker.adapter.SearchItemAdapter
import hu.bme.aut.android.portfoliotracker.databinding.FragmentFavoritesBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.BestStockSearchMatches
import hu.bme.aut.android.portfoliotracker.stockInfo.model.StockSearchItem
import hu.bme.aut.android.portfoliotracker.stockInfo.network.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FavoritesFragment : Fragment(), SearchItemAdapter.SearchItemClickListener {
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var adapter: SearchItemAdapter

    private lateinit var swipeToAction: SwipeToAction

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        initRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.suggestionsAdapter =
            SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
                intArrayOf(
                    android.R.id.text1
                ), FLAG_REGISTER_CONTENT_OBSERVER
            )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.length >= 2) {
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText?.length!! >= 2) {
                    loadStockSearchItems(newText)

                } else {
                    activity?.runOnUiThread{
                        binding.searchView.suggestionsAdapter.changeCursor(null)
                    }
                }
                return true
            }

        })

        binding.searchView.setOnSuggestionListener(object :
            android.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                val cursor = binding.searchView.suggestionsAdapter.getItem(position) as Cursor
                val searchName = cursor.getString(1)
                val symbol = searchName.substringBefore("-").trim()
                val stockName = searchName.substringAfter("-").trim()

                val alertDialog: AlertDialog.Builder =
                    AlertDialog.Builder(this@FavoritesFragment.context)
                alertDialog.setTitle("Add to favorites?")
                alertDialog.setMessage("Do you wish to add the tracker \"$searchName\" to your favorites?")
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    run {
                        val stockSearchItem = StockSearchItem(symbol, stockName, "Equity")
                        addItem(null,stockSearchItem)
                        Snackbar.make(binding.root,"Tracker added",Snackbar.LENGTH_LONG).show()
                    }
                }
                alertDialog.setNegativeButton(
                    "No"
                ) { _, _ -> }
                val alert: AlertDialog = alertDialog.create()
                alert.setCanceledOnTouchOutside(false)
                alert.show()

                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                return onSuggestionSelect(position)
            }

        })

    }

    private fun loadStockSearchItems(keyword: String) {
        NetworkManager.getStockSearchItem(keyword)
            .enqueue(object : Callback<BestStockSearchMatches?> {
                override fun onResponse(
                    call: Call<BestStockSearchMatches?>,
                    response: Response<BestStockSearchMatches?>
                ) {
                    Log.d(TAG, "onResponse: " + response.code())
                    if (response.isSuccessful) {
                        val cursor: MatrixCursor
                        val sAutocompleteColNames = arrayOf(
                            BaseColumns._ID,
                            SearchManager.SUGGEST_COLUMN_TEXT_1
                        )
                        cursor = MatrixCursor(sAutocompleteColNames)

                        val matches = response.body()?.bestMatches

                        if (matches != null) {
                            var counter=0
                            for (index in matches.indices) {
                                if(matches[index].type!! != "Equity")
                                    continue
                                if(User.favorites.find { s -> s.symbol == matches[index].symbol!! } != null)
                                    continue
                                if (counter >= 4)
                                    break

                                counter++

                                val name: String = matches[index].name!!
                                val symbol: String = matches[index].symbol!!
                                val searchItem = "$symbol - $name"
                                val row = arrayOf<Any>(index, searchItem)
                                cursor.addRow(row)
                            }

                            activity?.runOnUiThread{
                                binding.searchView.suggestionsAdapter.changeCursor(cursor)
                            }
                        } else {
                            activity?.runOnUiThread{
                                binding.searchView.suggestionsAdapter.changeCursor(null)
                            }
                        }
                    } else {
                        Snackbar.make(binding.root,"Error: " + response.message(),Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: Call<BestStockSearchMatches?>,
                    throwable: Throwable
                ) {
                    throwable.printStackTrace()
                    Snackbar.make(binding.root,"Network request error occurred, check LOG",Snackbar.LENGTH_LONG).show()
                }
            })
    }

    private fun initRecyclerView() {
        adapter = SearchItemAdapter()
        binding.rvMain.layoutManager = LinearLayoutManager(this.context)
        binding.rvMain.adapter = adapter
        binding.rvMain.addOnItemTouchListener(object: RecyclerView.OnItemTouchListener{
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }

        })

        swipeToAction = SwipeToAction(binding.rvMain, object : SwipeToAction.SwipeListener<StockSearchItem> {
            override fun swipeLeft(itemData: StockSearchItem): Boolean {
                val pos: Int = removeItem(itemData)
                displaySnackbar(itemData.symbol.toString() + " removed", "Undo"
                ) { addItem(pos, itemData) }
                return true
            }

            override fun swipeRight(itemData: StockSearchItem?): Boolean {
                return true
            }

            override fun onClick(itemData: StockSearchItem?) {
                displaySnackbar(itemData?.symbol.toString() + " Info Page", null, null)
                val bundle = Bundle()
                bundle.putString("symbol", itemData?.symbol)
                findNavController().navigate(R.id.action_mainPageFragment_to_infoFragment,bundle)

            }

            override fun onLongClick(itemData: StockSearchItem?) {
                val stock = User.stocksOwned.find { x->x.symbol == itemData?.symbol }
                if( stock != null){
                    val bundle = Bundle()
                    bundle.putString("symbol",stock.symbol)
                    findNavController().navigate(R.id.action_mainPageFragment_to_stockFragment,bundle)
                }
                else{
                    displaySnackbar(itemData?.symbol.toString() + ": You do not own shares from this stock", null, null)
                }

            }
        })

        loadItemsInBackground()

    }

    private fun loadItemsInBackground() {
        activity?.runOnUiThread{
            for(stock in User.favorites){
                adapter.addItem(stock)
            }
        }
    }



    private fun removeItem(item: StockSearchItem): Int {
        val pos: Int = adapter.getIndex(item)
        activity?.runOnUiThread{
            adapter.removeItem(item)
            User.favorites.remove(item)
        }
        lifecycleScope.launch(Dispatchers.Main){
            withContext(Dispatchers.IO){
                User.database.stockFavoriteDAO().deleteItem(item)
            }
        }
        return pos
    }

    private fun addItem(pos: Int?, item: StockSearchItem) {
        activity?.runOnUiThread{
            if( pos == null) {
                adapter.addItem(item)
                User.favorites.add(item)
            }
            else {
                adapter.addItem(pos, item)
                User.favorites.add(pos, item)
            }
        }
        lifecycleScope.launch(Dispatchers.Main){
            withContext(Dispatchers.IO){
                User.database.stockFavoriteDAO().insert(item)
            }
        }
    }

    private fun displaySnackbar(text: String, actionName: String?, action: View.OnClickListener?) {
        val typedValueColor = TypedValue()
        val typedValueBackground = TypedValue()

        val aColor: TypedArray =
            binding.root.context.obtainStyledAttributes(typedValueColor.data, intArrayOf(android.R.attr.textColorPrimaryInverse))
        val color = aColor.getColor(0, 0)
        aColor.recycle()

        val aBackground: TypedArray =
            binding.root.context.obtainStyledAttributes(typedValueBackground.data, intArrayOf(android.R.attr.colorPrimary))
        val colorBackground = aBackground.getColor(0, 0)
        aBackground.recycle()


        val snack = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            .setAction(actionName, action)
        val v = snack.view
        v.setBackgroundColor(colorBackground)
        (v.findViewById(com.google.android.material.R.id.snackbar_text) as TextView).setTextColor(
            color
        )
        (v.findViewById(com.google.android.material.R.id.snackbar_action) as TextView).setTextColor(
            color
        )
        snack.show()
    }

    override fun onItemChanged(item: StockSearchItem) {
        //Do nothing
    }


}