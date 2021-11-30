package lej.happy.fooddiary.data

import io.reactivex.rxjava3.subjects.PublishSubject
import lej.happy.fooddiary.data.model.BarDate

object UpdateEvent {

    val mRefreshUpdateDataObs: PublishSubject<Boolean> = PublishSubject.create()

    val mDateUpdateDataObs: PublishSubject<BarDate> = PublishSubject.create()

    val mOrderUpdateDataObs: PublishSubject<String> = PublishSubject.create()
}