package org.jetbrains.demo.thinkter

import kotlinx.html.*
import org.jetbrains.ktor.html.*

class ApplicationPage : Template<HTML> {
    val caption = Placeholder<TITLE>()
    val head = Placeholder<HEAD>()

    override fun HTML.apply() {
        classes += "mdc-typography"
        head {
            meta { charset = "utf-8" }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0"
            }
            title {
                insert(caption)
            }
            insert(head)
            link("https://fonts.googleapis.com/icon?family=Material+Icons", rel = "stylesheet")

            link(rel = LinkRel.stylesheet, type=LinkType.textCss, href = "http://yui.yahooapis.com/pure/0.6.0/pure-min.css")
            link(rel = LinkRel.stylesheet, type=LinkType.textCss, href = "http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css")

        }
        body {
            script {
                unsafe {
                    +"""
    var WebFontConfig = {
      google: { families: [ 'Roboto:400,300,500:latin' ] }
    };
    (function() {
      var wf = document.createElement('script');
      wf.src = ('https:' == document.location.protocol ? 'https' : 'http') +
      '://ajax.googleapis.com/ajax/libs/webfont/1/webfont.js';
      wf.type = 'text/javascript';
      wf.async = 'true';
      var s = document.getElementsByTagName('script')[0];
      s.parentNode.insertBefore(wf, s);
    })();
"""
                }
            }
            div { id = "content" }
            script(src = "frontend/frontend.bundle.js")
        }
    }
}
