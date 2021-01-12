package com.example.android.storemanagement


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.getCurrentFirebaseUserInternal
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.fragment_users.view.*


open class UserSelectionFragment : Fragment() {

    private var listener: OnNavigationChangedListener? = null
    private var users = mutableListOf<FirebaseUserInternal>()

    lateinit var onNavigationChangedListener: OnNavigationChangedListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_users,
            container,
            false
        )
        view.toolbarTop.setNavigationIcon(R.drawable.ic_baseline_arrow_back)
        view.toolbarTop.setNavigationOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        getCurrentFirebaseUserInternal { firebaseUser ->

            when (firebaseUser.accountType) {
                UserType.VENDOR.toString() -> {
                    getFirebaseUsers(UserType.VENDOR)
                    toolbarTop.title = context?.getString(R.string.vendors)
                }
                UserType.RETAILER.toString() -> {
                    getFirebaseUsers(UserType.VENDOR)
                    toolbarTop.title = context?.getString(R.string.vendors)
                }
                UserType.CUSTOMER.toString() -> {
                    getFirebaseUsers(UserType.RETAILER)
                    toolbarTop.title = context?.getString(R.string.retailers)
                }
            }
            setupRecyclerView(users)
        }
    }

//    private fun getFirebaseUsers() {
//        val user: FirebaseUser? = Firebase.auth.currentUser
//        val uniqueId: String = user?.uid!!
//        val database = FirebaseDatabase.getInstance()
//        val usersQuery: Query = database.getReference("Users")
//
//        usersQuery.add(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (appleSnapshot in dataSnapshot.children) {
//                    val firebaseUser =
//                        dataSnapshot.getValue(FirebaseUserInternal::class.java)
////                    if (firebaseUser?.id != uniqueId) {
//                    users.add(firebaseUser!!)
////                    }
//                }
//                setupRecyclerView(users)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//            }
//        })
//        setupRecyclerView(users)
//    }

    private fun getFirebaseUsers(accountType: UserType) {
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database = FirebaseDatabase.getInstance()
        val usersQuery: Query = database.getReference("Users").orderByChild("accountType").equalTo(accountType.toString())
        usersQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val firebaseUser =
                    dataSnapshot.getValue(FirebaseUserInternal::class.java)
                if (users.none { it.id == firebaseUser?.id } && firebaseUser?.id != uniqueId) {
                    users.add(firebaseUser!!)
                }
                activity?.runOnUiThread { setupRecyclerView(users) }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        activity?.runOnUiThread { setupRecyclerView(users) }
    }

    private fun openCreateOrderFragment(firebaseUser: FirebaseUserInternal) {
        if (::onNavigationChangedListener.isInitialized) {
            onNavigationChangedListener.onNavigationChanged(tabNumber = CREATE_ORDER_TAB, firebaseUser = firebaseUser)
            listener = onNavigationChangedListener
        }

        listener?.onNavigationChanged(tabNumber = CREATE_ORDER_TAB, firebaseUser = firebaseUser)
        parentFragmentManager.popBackStackImmediate()
    }

    private fun setupEmptyView() {
        val users = users_recycler_view?.adapter
        if (users?.itemCount == 0) {
            users_recycler_view?.visibility = View.GONE
            empty_view_users?.visibility = View.VISIBLE
        } else {
            users_recycler_view?.visibility = View.VISIBLE
            empty_view_users?.visibility = View.GONE
        }
    }

    private fun setupRecyclerView(firebaseUsers: List<FirebaseUserInternal>) {
        users_recycler_view?.layoutManager =
            LinearLayoutManager(requireContext())
        val usersAdapter = UsersAdapter(
            requireContext(), ::openCreateOrderFragment
        )
        users_recycler_view?.adapter = usersAdapter

        usersAdapter.setUsers(firebaseUsers)
        setupEmptyView()
    }
}