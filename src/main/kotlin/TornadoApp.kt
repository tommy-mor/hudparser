import javafx.collections.ObservableList
import tornadofx.*

data class Transfer(val from: Hud, val to: Hud, val element: String) {
    override fun toString(): String {
        return "import ${element.toUpperCase()} from ${from.toString().toUpperCase()} into ${to.toString().toUpperCase()}"
    }
}

class MyView: View() {
    private var huds = mutableListOf(
            Hud("/Users/tommy/Downloads/rayshud-master")
    ).observable()
    var selectedHud: Hud? = null
    var selectedElement: String? = null
    var baseHud: Hud? = null
    var transferList: ObservableList<Transfer> = mutableListOf<Transfer>().observable()
    var selectedTransfer: Transfer? = null

    var errorText by property<String>()
    fun errorTextProperty() = getProperty(MyView::errorText)

    override val root = vbox {
        label("hud mixer tool")
        hbox {
            vbox {
                label("from")
                //todo make list of huds save
                //todo make table view with checkmark for if it parsed correctly
                tableview(huds) {
                    readonlyColumn("Name", Hud::hudname)
                    readonlyColumn("Parsed", Hud::parsed)
                    setOnMouseClicked { selectedHud = selectedItem }
                }
                hbox {
                    button("+") {
                        action {
                            errorTextProperty().set("")
                            val file = chooseDirectory("Select HUD directory")
                            file?.let {
                                val hud = Hud(it.absolutePath)
                                huds.add(hud)
                            }
                        }
                    }
                    button("-") {
                        action {
                            errorTextProperty().set("")
                            if(selectedHud != baseHud) huds.remove(selectedHud)
                        }
                    }
                }
            }

            vbox {
                label("import")
                listview(listOf("health bar", "damage numbers",
                        "ammo").observable()) {
                    setOnMouseClicked { selectedElement = selectedItem }
                }
            }
            vbox {
                label("into")
                combobox<Hud> {
                    items = huds
                    setOnAction {
                        baseHud = selectedItem
                    }

                    disableProperty().bind(transferList.sizeProperty.gt(0))
                }
            }
        }
        button("make transfer") {
            action {
                errorTextProperty().set("")
                try {
                    if(selectedHud == baseHud) throw NonsensicalException("cannot transfer a component into its own hud")
                    transferList.add(Transfer(selectedHud ?: throw NotAllSelectedException("component hud"),
                            baseHud ?: throw NotAllSelectedException("base hud"),
                            selectedElement?: throw NotAllSelectedException("element")))
                } catch(exception: NotAllSelectedException) {
                    errorText = "${exception.message} not selected"
                } catch(exception: NonsensicalException) {
                    errorText = exception.message
                }
            }
        }

        listview<Transfer>(transferList) {
            this.setMaxSize(Double.MAX_VALUE, 100.0)
            setOnMouseClicked { selectedTransfer = selectedItem}
        }

        button("-") {
            action {
                errorTextProperty().set("")
                selectedTransfer?.let { transferList.remove(it) }
            }
        }

        button("create") {
            action {
                errorTextProperty().set("")
            }
        }
        label(errorTextProperty())
    }

    init {
    }
}

class NotAllSelectedException(message: String) : Exception(message)
class NonsensicalException(message: String): Exception(message)