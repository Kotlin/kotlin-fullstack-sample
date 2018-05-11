package org.jetbrains.demo.thinkter

import kotlinx.html.div
import org.jetbrains.demo.thinkter.enzyme.Adapter
import org.jetbrains.demo.thinkter.enzyme.configure
import org.jetbrains.demo.thinkter.enzyme.shallow
import org.jetbrains.demo.thinkter.model.Thought
import react.dom.buildElement
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ExampleTest {
    @BeforeTest
    fun configure() {
        configure {
            adapter = Adapter()
        }
    }

    @Test
    fun smokeTest() {
        val w = shallow(buildElement {
            div {
                ViewThoughtComponent {
                    thought = Thought(1, "aaa", "test text", "now", null)
                }
            }
        })

        assertTrue { w.find("ViewThoughtComponent").exists() }
    }
}


