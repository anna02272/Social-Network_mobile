import com.example.socialnetwork.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class PostAdapter(private val mContext: Context, posts: ArrayList<Post>) :
    ArrayAdapter<Post?>(mContext, 0, posts as List<Post?>) {
    private val mPosts: ArrayList<Post>

    init {
        mPosts = posts
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val post: Post? = getItem(position)

        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.activity_post, parent, false)
        }

        val profileImage = convertView!!.findViewById<ImageView>(R.id.profileImage)
        val usernameTextView = convertView.findViewById<TextView>(R.id.usernameTextView)
        val dateTextView = convertView.findViewById<TextView>(R.id.dateTextView)
        val contentTextView = convertView.findViewById<TextView>(R.id.contentTextView)

        post?.let {
            profileImage.setImageResource(it.getProfileImageResource())
            usernameTextView.text = it.getUsername()
            dateTextView.text = it.getDate()
            contentTextView.text = it.getContent()
        }

        return convertView
    }
}
