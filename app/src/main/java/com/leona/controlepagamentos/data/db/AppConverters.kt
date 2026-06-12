package com.leona.controlepagamentos.data.db

import androidx.room.TypeConverter
import com.leona.controlepagamentos.data.model.CaptureStatus
import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.PaymentSource
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.data.model.PaymentType
import com.leona.controlepagamentos.data.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

class AppConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun paymentTypeToString(value: PaymentType?): String? = value?.name

    @TypeConverter
    fun stringToPaymentType(value: String?): PaymentType? = value?.let(PaymentType::valueOf)

    @TypeConverter
    fun paymentStatusToString(value: PaymentStatus?): String? = value?.name

    @TypeConverter
    fun stringToPaymentStatus(value: String?): PaymentStatus? = value?.let(PaymentStatus::valueOf)

    @TypeConverter
    fun paymentMethodToString(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun stringToPaymentMethod(value: String?): PaymentMethod? = value?.let(PaymentMethod::valueOf)

    @TypeConverter
    fun paymentSourceToString(value: PaymentSource?): String? = value?.name

    @TypeConverter
    fun stringToPaymentSource(value: String?): PaymentSource? = value?.let(PaymentSource::valueOf)

    @TypeConverter
    fun captureStatusToString(value: CaptureStatus?): String? = value?.name

    @TypeConverter
    fun stringToCaptureStatus(value: String?): CaptureStatus? = value?.let(CaptureStatus::valueOf)

    @TypeConverter
    fun parseConfidenceToString(value: ParseConfidence?): String? = value?.name

    @TypeConverter
    fun stringToParseConfidence(value: String?): ParseConfidence? = value?.let(ParseConfidence::valueOf)

    @TypeConverter
    fun transactionTypeToString(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): TransactionType? = value?.let(TransactionType::valueOf)
}
