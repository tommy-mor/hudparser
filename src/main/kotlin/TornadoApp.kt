import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import tornadofx.*
import javax.json.JsonArray
import javax.xml.bind.JAXBElement

//todo make it load in nicer
//todo fix resizing behavior

data class Transfer(val from: Hud, val element: String) {
    override fun toString(): String {
        return "${element} from ${from.toString()}"
    }
}

class MyView: View() {
    private var huds: ObservableList<Hud> = (config.string("hudlist")).split(", ").distinct().map { Hud(it) }.observable()

    var selectedHud: Hud? = null
    var selectedElement: String? = null
    var transferList: ObservableList<Transfer> = mutableListOf<Transfer>().observable()
    var selectedTransfer: Transfer? = null
    lateinit var scrollpane: ScrollPane

    var baseHud: Hud? by property<Hud>()
    fun baseHudProperty() = getProperty(MyView::baseHud)

    var logString by property<String>("")
    fun logStringProperty() = getProperty(MyView::logString)

    var errorText by property<String>()
    fun errorTextProperty() = getProperty(MyView::errorText)

    val spec: Spec = parseSpec("/Users/tommy/programming/parser/src/main/resources/features.txt")

    private fun log(msg: String) {
        logStringProperty().set(logStringProperty().get() + msg)
        logStringProperty().set(logStringProperty().get() + "\n")
    }

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
                                log("adding hud at ${it.absolutePath}")
                                val hud = Hud(it.absolutePath)
                                huds.add(hud)
                                config.set("hudlist" to huds.map { it.rootfile.absolutePath }.distinct().joinToString(", "))
                                config.save()
                            }
                        }
                    }
                    button("-") {
                        action {
                            errorTextProperty().set("")
                            if (selectedHud != baseHud) huds.remove(selectedHud)
                            log("removing hud at ${selectedHud?.rootfile?.absolutePath}")
                            config.set("hudlist" to huds.map { it.rootfile.absolutePath }.distinct().joinToString(", "))
                            config.save()
                        }
                    }
                    button("select as base") {
                        action {
                            errorTextProperty().set("")
                            if (selectedHud == null) {
                                errorText = "no hud selected"
                            } else if (transferList.any { it.from == selectedHud }) {
                                errorText = "please remove transfers coming from ${selectedHud?.hudname}"
                            } else {
                                baseHud = selectedHud
                            }
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
                alignment = Pos.CENTER
                button("====>") {
                    action {
                        errorTextProperty().set("")
                        try {
                            val transfer = Transfer(selectedHud ?: throw NotAllSelectedException("component hud"),
                                    selectedElement ?: throw NotAllSelectedException("element"))

                            if (selectedHud == baseHud) throw NonsensicalException("cannot transfer a component into its own hud")
                            if (transferList.contains(transfer)) throw NonsensicalException("cannot transfer element twice")
                            transferList.add(transfer)
                        } catch (exception: NotAllSelectedException) {
                            errorText = "${exception.message} not selected"
                        } catch (exception: NonsensicalException) {
                            errorText = exception.message
                        }
                    }
                }
                button("<====") {
                    action {
                        errorTextProperty().set("")
                        selectedTransfer?.let { transferList.remove(it) }
                    }
                }
            }
            vbox {
                label("Base Hud:")
                label(baseHudProperty())
                listview<Transfer>(transferList) {
                    setOnMouseClicked { selectedTransfer = selectedItem }
                }
            }
        }

        vbox {
            scrollpane {
                //warning. this is jank. its the only way I could get the scroll bar to stay down
                scrollpane = this
                var vbox: VBox? = null
                vbox {
                    vbox = this
                    label(logStringProperty())
                }
                vvalueProperty().bind(vbox!!.heightProperty())

                this.minViewportHeight = 130.0
                this.maxHeight = 130.0
            }


            button("create") {
                action {
                    errorTextProperty().set("")
                    log("test create")
                }
            }
            label(errorTextProperty())
        }
    }

    init {
        log("loading previously selected huds: ${config.string("hudlist")}")
    }
}

class NotAllSelectedException(message: String) : Exception(message)
class NonsensicalException(message: String): Exception(message)