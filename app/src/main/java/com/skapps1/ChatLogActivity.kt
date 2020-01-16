package com.skapps1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter


          //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)


        supportActionBar?.title = toUser?.username



        //setupDummyData()
        listenForMessages()


        sendbutton_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            performSendMessage()
        }




    }

    private fun listenForMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        //this guy below is going to notify us for every messages in the node
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                //we will work on this
              val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage != null){
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessages.currentUser ?:return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }




                }

                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
        constructor() : this("", "", "", "", -1)
    }
   private fun performSendMessage() {
        //send message to firebase


       val text = edittext_chat_log.text.toString()
       val fromId = FirebaseAuth.getInstance().uid
       val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
       val toId = user.uid

       if (fromId == null) return
      // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
       //.push() is for creating a new node

       val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

       val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()


       val chatMessage = ChatMessage(reference.key!!, text,fromId, toId, System.currentTimeMillis() /1000 ) // iOS gives seconds normally

       reference.setValue(chatMessage).addOnSuccessListener {
           Log.d(TAG, "Saved our chat message: ${reference.key}")
           edittext_chat_log.text.clear()
           recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
       }

       toReference.setValue(chatMessage)

       val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId") //there is no push here
       latestMessageRef.setValue(chatMessage)

       val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId") //there is no push here
       latestMessageToRef.setValue(chatMessage)
    }


}


class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text

        //load our user image onto the pic
        val uri  = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_from_chat
        Picasso.get().load(uri).into(targetImageView)

    }
    override fun getLayout(): Int { ///row that renders chat rows
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_to.text = text


        val uri  = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView2_to_chat_row
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int { ///row that renders chat rows
        return R.layout.chat_to_row
    }
}
