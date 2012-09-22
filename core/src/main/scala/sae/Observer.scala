package sae

trait Observer[-V] {

    def updated(oldV : V, newV : V)

    def removed(v : V)

    def added(v : V)

}