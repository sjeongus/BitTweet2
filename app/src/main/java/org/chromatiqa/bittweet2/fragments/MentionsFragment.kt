package org.chromatiqa.bittweet2.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.content_home.view.*
import org.chromatiqa.bittweet2.R
import org.chromatiqa.bittweet2.adapters.TweetsAdapter
import org.chromatiqa.bittweet2.utils.TinyDB

class MentionsFragment : Fragment() {
    val LINEAR_LAYOUT_STATE : String = "myLinearLayout"

    var layoutManager : LinearLayoutManager? = null
    var listState : Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        super.onCreate(savedInstanceState)

        activity.title = "Mentions"

        val username = activity.getSharedPreferences("users", Context.MODE_PRIVATE).getString("current_user", null)
        val prefs = activity.getSharedPreferences(username, Context.MODE_PRIVATE)
        val userId = prefs.getLong("userId", 0)
        val tinydb = TinyDB(context)

        val linearLayout : LinearLayout = inflater?.inflate(R.layout.content_mentions, container, false) as LinearLayout
        val adapter = TweetsAdapter(activity, userId, this@MentionsFragment)
        val rv = linearLayout.rv
        layoutManager = LinearLayoutManager(activity.baseContext)
        rv.layoutManager = layoutManager
        rv.adapter = adapter

        if (adapter.itemCount == 0) {
            val tweets = tinydb.getListObject(username + "mentions_timeline", Tweet::class.java) as List<Tweet>
            if (tweets.size > 0) {
                adapter.putTweets(tweets)
            } else {
                refresh(linearLayout.swipe, adapter, tinydb, username)
            }
        }

        val refreshLayout = linearLayout.swipe

        linearLayout.swipe.setOnRefreshListener {
            refresh(refreshLayout, adapter, tinydb, username)
        }

        return linearLayout
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)

        listState = layoutManager?.onSaveInstanceState()
        state.putParcelable(LINEAR_LAYOUT_STATE, listState)
    }

    override fun onActivityCreated(state: Bundle?) {
        super.onActivityCreated(state)

        if (state != null) {
            listState = state.getParcelable(LINEAR_LAYOUT_STATE)
        }
    }

    override fun onResume() {
        super.onResume()

        if (listState != null) {
            layoutManager?.onRestoreInstanceState(listState)
        }
    }

    fun refresh(refreshLayout: SwipeRefreshLayout, adapter: TweetsAdapter, tinydb: TinyDB, username: String) {
        val twitterApiClient = TwitterCore.getInstance().apiClient
        val statusesService = twitterApiClient.statusesService
        val lastId = adapter.getTopId()
        statusesService.mentionsTimeline(200, lastId, null, false, true, true, object: Callback<List<Tweet>>() {
            override fun success(result: Result<List<Tweet>>) {
                adapter.putTweets(result.data)
                val temp = tinydb.getListObject(username + "mentions_timeline", Tweet::class.java)
                temp.addAll(0, result.data)
                tinydb.putListObject(username + "mentions_timeline", temp)
                refreshLayout.isRefreshing = false
            }

            override fun failure(e: TwitterException) {
                e.printStackTrace()
                refreshLayout.isRefreshing = false
            }
        })
    }
}
