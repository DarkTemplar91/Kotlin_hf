package hu.bme.aut.android.portfoliotracker.stockInfo.network

import android.content.ContentValues
import android.util.Log
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.portfoliotracker.databinding.FragmentInfoBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.ApiContainerInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class CallbackClass<T: ApiContainerInterface>(binding: FragmentInfoBinding, function: ((T?) -> Unit)?) : Callback<T?> {
    private val binding: FragmentInfoBinding
    private val function: ((T?) -> Unit)?

    init{
        this.binding = binding
        this.function = function
    }

    override fun onResponse(call: Call<T?>, response: Response<T?>) {
        if (response.isSuccessful) {
            val responseBody = response.body()

            if(responseBody?.Note != null)
            {
                Snackbar.make(binding.root,"Error: Could not fetch data. Free API Calls may have been used up",
                    Snackbar.LENGTH_LONG).show()

                binding.progressBar.isVisible = false
                binding.swiperefresh.isVisible = true

            }
            else {
                function?.invoke(responseBody)
            }
        } else {
            Log.d(ContentValues.TAG, "Error: " + response.message())
            Snackbar.make(binding.root,"Error: Something went wrong", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onFailure(call: Call<T?>, t: Throwable) {
        t.printStackTrace()
        Snackbar.make(binding.root,"Network request error occurred, check LOG",Snackbar.LENGTH_LONG).show()
    }
}