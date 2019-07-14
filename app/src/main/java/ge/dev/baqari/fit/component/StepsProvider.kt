package ge.dev.baqari.fit.component

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import ge.dev.baqari.fit.component.StepsProvider.StepColumns.ALL_STEPS

import ge.dev.baqari.fit.model.StepModel

import io.realm.Realm
import io.realm.RealmResults
import java.text.SimpleDateFormat


class StepsProvider : ContentProvider() {

    override fun getType(uri: Uri): String? {
        when (uriMatcher.match(uri)) {
            ALL_STEPS -> return "vnd.android.cursor.dir/vnd.example.all_steps"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    @SuppressLint("SimpleDateFormat")
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val realm = Realm.getDefaultInstance()
        val match = uriMatcher.match(uri)
        val stepsCursor = MatrixCursor(arrayOf("numSteps", "startDate", "endDate"))
        realm.use {
            when (match) {
                ALL_STEPS -> {
                    var realmTask: RealmResults<StepModel>? = null
                    if (uri.pathSegments?.size!! > 0) {
                        val startDateParam = SimpleDateFormat("dd-MM-yyyy").parse(uri.pathSegments[1])
                        val endDateParam = SimpleDateFormat("dd-MM-yyyy").parse(uri.pathSegments[2])
                        realmTask = if (startDateParam != null || endDateParam != null)
                            it.where(StepModel::class.java)
                                    .greaterThanOrEqualTo("startDate", startDateParam!!)
                                    .lessThanOrEqualTo("endDate", endDateParam!!)
                                    .findAll()
                        else
                            it.where(StepModel::class.java).findAll()

                        realmTask.forEach { result ->
                            val startDate = SimpleDateFormat("dd-MM-yyyy HH:mm").format(result.startDateTime!!).toString()
                            val endDate = SimpleDateFormat("dd-MM-yyyy HH:mm").format(result.endDateTime!!).toString()
                            val rowData = arrayOf<Any>(result.numSteps, startDate, endDate)
                            stepsCursor.addRow(rowData)
                        }
                    }
                }
                else -> throw UnsupportedOperationException("Unknown uri: $uri")
            }
            stepsCursor.setNotificationUri(context?.contentResolver, uri)
        }
        return stepsCursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    object StepColumns {
        val ALL_STEPS = -1
    }

    companion object {
        val PROVIDER_NAME = "com.mygpi.mygpimobilefitness.StepsProvider"
        val URL = "content://$PROVIDER_NAME/steps"
        val CONTENT_AUTHORITY = Uri.parse(URL)

        internal val uriCode = 1
        internal val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(PROVIDER_NAME, "steps", uriCode)
            uriMatcher.addURI(PROVIDER_NAME, "steps/#", uriCode)
        }
    }
}