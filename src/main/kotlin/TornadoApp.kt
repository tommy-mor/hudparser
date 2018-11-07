import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.collections.transformation.TransformationList
import javafx.event.EventHandler
import javafx.scene.control.SelectionMode
import tornadofx.*
import javafx.scene.paint.Color
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator
import java.util.stream.Stream


class MyView: View() {
    private var huds = mutableListOf(
            Hud("/Users/tommy/Downloads/rayshud-master")
    ).observable()
    private var hudstrings: ObservableList<String> = FXCollections.observableArrayList(huds.map { it.hudname })
    var selectedHud: Hud? = null
    private lateinit var baseHud: Hud
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
                    onUserSelect {
                        println(it)
                        selectedHud = it
                    }
                    setOnMouseClicked { selectedHud = selectedItem }

                }
                hbox {
                    button("+") {
                        action {
                            val file = chooseDirectory("Select HUD directory")
                            file?.let {
                                val hud = Hud(it.absolutePath)
                                huds.add(hud)
                            }
                        }
                    }
                    button("-") {
                        action {
                            huds.remove(selectedHud)
                        }
                    }
                }
            }

            vbox {
                label("import")
                listview(listOf("health bar", "damage numbers",
                        "ammo").observable())
            }
            vbox {
                label("into")
                combobox<String> {
                    items = hudstrings
                }
            }
        }
        button("make transfer")
        listview(listOf("import HEALTH from RAYSHUD into BASEHUD").observable()) {
            this.setMaxSize(Double.MAX_VALUE, 100.0)
            this.isFocusTraversable = false
        }
        button("create")
    }


    init {
        huds.addListener(ListChangeListener { c ->
            while(c.next()) {
                if(c.wasAdded()) {
                    val list = c.addedSubList.asSequence().toList()
                    hudstrings.add(list[0].hudname)
                } else if (c.wasRemoved()) {
                    hudstrings.remove(c.removed.asSequence().toList()[0].hudname)
                }
            }
        })
    }
}