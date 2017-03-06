package de.eiswind.xino.ui.vaadin

import com.vaadin.data.Binder
import com.vaadin.data.ValueProvider
import com.vaadin.server.Setter
import kotlin.reflect.KMutableProperty

/**
 * Created by thomas on 15.01.17.
 */
fun <BEAN, T> Binder.BindingBuilder<BEAN, T>.bind(prop: KMutableProperty<T>) {
    this.bind(
            ValueProvider { bean: BEAN -> prop.getter.call(bean) },
            Setter { bean: BEAN, v: T -> prop.setter.call(bean, v) })
}