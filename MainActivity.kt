import DataClass
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    private val userCollectionReference = Firebase.firestore.collection("User")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        submit.setOnClickListener { onClickedOnMyButton() }

        returnData.setOnClickListener { showResultFromFirebase() }
    }


    private suspend fun showToastInMainActivity(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveData(dataClass: DataClass) = CoroutineScope(Dispatchers.IO).launch {

        if (isActive) {
            launch {
                try {
                    userCollectionReference.add(dataClass).await()
                    showToastInMainActivity("You have successfully registered this user")
                    yield()
                } catch (ex: Exception) {
                    ex.message?.let { showToastInMainActivity(it) }
                }
            }
        } else {
            showToastInMainActivity("Time Out")
        }
    }

    private fun onClickedOnMyButton() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userName: String = userName.text.toString().trim()
            val userFatherName: String = fatherName.text.toString().trim()
            val userMotherName: String = motherName.text.toString().trim()

            val dataClass = DataClass(userName, userFatherName, userMotherName)
            saveData(dataClass).join()
            yield()
        } catch (ex: Exception) {
            ex.message?.let { showToastInMainActivity(it) }
        }
    }

    private fun showResultFromFirebase() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = userCollectionReference.get().await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<DataClass>()
                sb.append("$person\n")
                yield()
            }
            withContext(Dispatchers.Main) {
                textView.text = sb.toString()
            }
        } catch (ex: Exception) {
            ex.message?.let { showToastInMainActivity(it) }
        }
    }

}
