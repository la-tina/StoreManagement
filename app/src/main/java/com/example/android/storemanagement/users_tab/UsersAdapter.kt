package com.example.android.storemanagement.users_tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.storemanagement.R
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import kotlinx.android.synthetic.main.user_item.view.*


class UsersAdapter(
    private val context: Context,
    private val openCreateOrderFragment: (FirebaseUserInternal) -> Unit
) :
    RecyclerView.Adapter<UsersHolder>() {

    companion object {
        private const val DRAWABLE = "drawable"
        private val imagesNamesList = listOf(
            "orange",
            "avocado",
            "banana",
            "blueberry",
            "cherry",
            "kiwi",
            "lemon",
            "apple",
            "raspberry",
            "watermelon",
            "pineapple",
            "peach",
            "pear"
        )
    }

    private var users = emptyList<FirebaseUserInternal>() // Cached copy of orders

    // Gets the number of items in the list
    override fun getItemCount(): Int = users.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersHolder =
        UsersHolder(LayoutInflater.from(context).inflate(R.layout.user_item, parent, false))

    override fun onBindViewHolder(holder: UsersHolder, position: Int) {
        val currentUser: FirebaseUserInternal = users[position]
        holder.email.text = currentUser.email
        holder.name.text = currentUser.name

        val imageName: String = if (position > imagesNamesList.size - 1) {
            getRandomImageName()
        } else {
            imagesNamesList[position]
        }
        val imageId = context.resources.getIdentifier(imageName, DRAWABLE, context.packageName)

        holder.image.setImageResource(imageId)
        holder.itemView.setOnClickListener {
            openCreateOrderFragment(currentUser)
        }
    }

    private fun getRandomImageName(): String {
        return imagesNamesList.shuffled().take(1)[0]
    }

    fun setUsers(users: List<FirebaseUserInternal>) {
        this.users = users
        notifyDataSetChanged()
    }
}

class UsersHolder(val view: View) : RecyclerView.ViewHolder(view) {
    // Holds the OrderTextView that will add each product to
    val name = view.user_name!!
    val email = view.user_email!!
    val image = view.imageView
}






