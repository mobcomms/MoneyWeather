import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserCertificationVO(
    @SerializedName("RSLT_BIRTHDAY") val birthday: String,
    @SerializedName("RSLT_CD") val code: String,
    @SerializedName("RSLT_MSG") val msg: String,
    @SerializedName("RSLT_NAME") val name: String,
    @SerializedName("RSLT_SEX_CD") val sexCode: String,
    @SerializedName("TEL_NO") val telNo: String,
    @SerializedName("CI") val ci: String
) : Serializable