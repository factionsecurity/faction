//
// ice - v0.5.0
// The MIT License
// Copyright (c) 2012 The New York Times, CMS Group, Matthew DeLambo <delambo@gmail.com> 
//
window.rangy = function() {
    function a(a, b) {
        var c = typeof a[b];
        return c == l || !(c != k || !a[b]) || "unknown" == c
    }
    function b(a, b) {
        return !(typeof a[b] != k || !a[b])
    }
    function c(a, b) {
        return typeof a[b] != m
    }
    function d(a) {
        return function(b, c) {
            for (var d = c.length; d--; )
                if (!a(b, c[d]))
                    return !1;
            return !0
        }
    }
    function e(a) {
        return a && r(a, q) && t(a, p)
    }
    function f(a) {
        window.alert("Rangy not supported in your browser. Reason: " + a),
        u.initialized = !0,
        u.supported = !1
    }
    function g(a) {
        var b = "Rangy warning: " + a;
        u.config.alertOnWarn ? window.alert(b) : typeof window.console != m && typeof window.console.log != m && window.console.log(b)
    }
    function h() {
        if (!u.initialized) {
            var c, d = !1, g = !1;
            a(document, "createRange") && (c = document.createRange(),
            r(c, o) && t(c, n) && (d = !0),
            c.detach());
            var h = b(document, "body") ? document.body : document.getElementsByTagName("body")[0];
            h && a(h, "createTextRange") && (c = h.createTextRange(),
            e(c) && (g = !0)),
            d || g || f("Neither Range nor TextRange are implemented"),
            u.initialized = !0,
            u.features = {
                implementsDomRange: d,
                implementsTextRange: g
            };
            for (var i = w.concat(v), j = 0, k = i.length; k > j; ++j)
                try {
                    i[j](u)
                } catch (l) {
                    b(window, "console") && a(window.console, "log") && window.console.log("Init listener threw an exception. Continuing.", l)
                }
        }
    }
    function i(a) {
        a = a || window,
        h();
        for (var b = 0, c = x.length; c > b; ++b)
            x[b](a)
    }
    function j(a) {
        this.name = a,
        this.initialized = !1,
        this.supported = !1
    }
    var k = "object"
      , l = "function"
      , m = "undefined"
      , n = ["startContainer", "startOffset", "endContainer", "endOffset", "collapsed", "commonAncestorContainer", "START_TO_START", "START_TO_END", "END_TO_START", "END_TO_END"]
      , o = ["setStart", "setStartBefore", "setStartAfter", "setEnd", "setEndBefore", "setEndAfter", "collapse", "selectNode", "selectNodeContents", "compareBoundaryPoints", "deleteContents", "extractContents", "cloneContents", "insertNode", "surroundContents", "cloneRange", "toString", "detach"]
      , p = ["boundingHeight", "boundingLeft", "boundingTop", "boundingWidth", "htmlText", "text"]
      , q = ["collapse", "compareEndPoints", "duplicate", "getBookmark", "moveToBookmark", "moveToElementText", "parentElement", "pasteHTML", "select", "setEndPoint", "getBoundingClientRect"]
      , r = d(a)
      , s = d(b)
      , t = d(c)
      , u = {
        version: "1.2.3",
        initialized: !1,
        supported: !0,
        util: {
            isHostMethod: a,
            isHostObject: b,
            isHostProperty: c,
            areHostMethods: r,
            areHostObjects: s,
            areHostProperties: t,
            isTextRange: e
        },
        features: {},
        modules: {},
        config: {
            alertOnWarn: !1,
            preferTextRange: !1
        }
    };
    u.fail = f,
    u.warn = g,
    {}.hasOwnProperty ? u.util.extend = function(a, b) {
        for (var c in b)
            b.hasOwnProperty(c) && (a[c] = b[c])
    }
    : f("hasOwnProperty not supported");
    var v = []
      , w = [];
    u.init = h,
    u.addInitListener = function(a) {
        u.initialized ? a(u) : v.push(a)
    }
    ;
    var x = [];
    u.addCreateMissingNativeApiListener = function(a) {
        x.push(a)
    }
    ,
    u.createMissingNativeApi = i,
    j.prototype.fail = function(a) {
        throw this.initialized = !0,
        this.supported = !1,
        new Error("Module '" + this.name + "' failed to load: " + a)
    }
    ,
    j.prototype.warn = function(a) {
        u.warn("Module " + this.name + ": " + a)
    }
    ,
    j.prototype.createError = function(a) {
        return new Error("Error in Rangy " + this.name + " module: " + a)
    }
    ,
    u.createModule = function(a, b) {
        var c = new j(a);
        u.modules[a] = c,
        w.push(function(a) {
            b(a, c),
            c.initialized = !0,
            c.supported = !0
        })
    }
    ,
    u.requireModules = function(a) {
        for (var b, c, d = 0, e = a.length; e > d; ++d) {
            if (c = a[d],
            b = u.modules[c],
            !(b && b instanceof j))
                throw new Error("Module '" + c + "' not found");
            if (!b.supported)
                throw new Error("Module '" + c + "' not supported")
        }
    }
    ;
    var y = !1
      , z = function() {
        y || (y = !0,
        u.initialized || h())
    };
    return typeof window == m ? (f("No window found"),
    void 0) : typeof document == m ? (f("No document found"),
    void 0) : (a(document, "addEventListener") && document.addEventListener("DOMContentLoaded", z, !1),
    a(window, "addEventListener") ? window.addEventListener("load", z, !1) : a(window, "attachEvent") ? window.attachEvent("onload", z) : f("Window does not have required addEventListener or attachEvent method"),
    u)
}(),
rangy.createModule("DomUtil", function(a, b) {
    function c(a) {
        var b;
        return typeof a.namespaceURI == z || null === (b = a.namespaceURI) || "http://www.w3.org/1999/xhtml" == b
    }
    function d(a) {
        var b = a.parentNode;
        return 1 == b.nodeType ? b : null
    }
    function e(a) {
        for (var b = 0; a = a.previousSibling; )
            b++;
        return b
    }
    function f(a) {
        var b;
        return j(a) ? a.length : (b = a.childNodes) ? b.length : 0
    }
    function g(a, b) {
        var c, d = [];
        for (c = a; c; c = c.parentNode)
            d.push(c);
        for (c = b; c; c = c.parentNode)
            if (D(d, c))
                return c;
        return null
    }
    function h(a, b, c) {
        for (var d = c ? b : b.parentNode; d; ) {
            if (d === a)
                return !0;
            d = d.parentNode
        }
        return !1
    }
    function i(a, b, c) {
        for (var d, e = c ? a : a.parentNode; e; ) {
            if (d = e.parentNode,
            d === b)
                return e;
            e = d
        }
        return null
    }
    function j(a) {
        var b = a.nodeType;
        return 3 == b || 4 == b || 8 == b
    }
    function k(a, b) {
        var c = b.nextSibling
          , d = b.parentNode;
        return c ? d.insertBefore(a, c) : d.appendChild(a),
        a
    }
    function l(a, b) {
        var c = a.cloneNode(!1);
        return c.deleteData(0, b),
        a.deleteData(b, a.length - b),
        k(c, a),
        c
    }
    function m(a) {
        if (9 == a.nodeType)
            return a;
        if (typeof a.ownerDocument != z)
            return a.ownerDocument;
        if (typeof a.document != z)
            return a.document;
        if (a.parentNode)
            return m(a.parentNode);
        throw new Error("getDocument: no document found for node")
    }
    function n(a) {
        var b = m(a);
        if (typeof b.defaultView != z)
            return b.defaultView;
        if (typeof b.parentWindow != z)
            return b.parentWindow;
        throw new Error("Cannot get a window object for node")
    }
    function o(a) {
        if (typeof a.contentDocument != z)
            return a.contentDocument;
        if (typeof a.contentWindow != z)
            return a.contentWindow.document;
        throw new Error("getIframeWindow: No Document object found for iframe element")
    }
    function p(a) {
        if (typeof a.contentWindow != z)
            return a.contentWindow;
        if (typeof a.contentDocument != z)
            return a.contentDocument.defaultView;
        throw new Error("getIframeWindow: No Window object found for iframe element")
    }
    function q(a) {
        return A.isHostObject(a, "body") ? a.body : a.getElementsByTagName("body")[0]
    }
    function r(a) {
        for (var b; b = a.parentNode; )
            a = b;
        return a
    }
    function s(a, b, c, d) {
        var f, h, j, k, l;
        if (a == c)
            return b === d ? 0 : d > b ? -1 : 1;
        if (f = i(c, a, !0))
            return b <= e(f) ? -1 : 1;
        if (f = i(a, c, !0))
            return e(f) < d ? -1 : 1;
        if (h = g(a, c),
        j = a === h ? h : i(a, h, !0),
        k = c === h ? h : i(c, h, !0),
        j === k)
            throw new Error("comparePoints got to case 4 and childA and childB are the same!");
        for (l = h.firstChild; l; ) {
            if (l === j)
                return -1;
            if (l === k)
                return 1;
            l = l.nextSibling
        }
        throw new Error("Should not be here!")
    }
    function t(a) {
        for (var b, c = m(a).createDocumentFragment(); b = a.firstChild; )
            c.appendChild(b);
        return c
    }
    function u(a) {
        if (!a)
            return "[No node]";
        if (j(a))
            return '"' + a.data + '"';
        if (1 == a.nodeType) {
            var b = a.id ? ' id="' + a.id + '"' : "";
            return "<" + a.nodeName + b + ">[" + a.childNodes.length + "]"
        }
        return a.nodeName
    }
    function v(a) {
        this.root = a,
        this._next = a
    }
    function w(a) {
        return new v(a)
    }
    function x(a, b) {
        this.node = a,
        this.offset = b
    }
    function y(a) {
        this.code = this[a],
        this.codeName = a,
        this.message = "DOMException: " + this.codeName
    }
    var z = "undefined"
      , A = a.util;
    A.areHostMethods(document, ["createDocumentFragment", "createElement", "createTextNode"]) || b.fail("document missing a Node creation method"),
    A.isHostMethod(document, "getElementsByTagName") || b.fail("document missing getElementsByTagName method");
    var B = document.createElement("div");
    A.areHostMethods(B, ["insertBefore", "appendChild", "cloneNode"] || !A.areHostObjects(B, ["previousSibling", "nextSibling", "childNodes", "parentNode"])) || b.fail("Incomplete Element implementation"),
    A.isHostProperty(B, "innerHTML") || b.fail("Element is missing innerHTML property");
    var C = document.createTextNode("test");
    A.areHostMethods(C, ["splitText", "deleteData", "insertData", "appendData", "cloneNode"] || !A.areHostObjects(B, ["previousSibling", "nextSibling", "childNodes", "parentNode"]) || !A.areHostProperties(C, ["data"])) || b.fail("Incomplete Text Node implementation");
    var D = function(a, b) {
        for (var c = a.length; c--; )
            if (a[c] === b)
                return !0;
        return !1
    };
    v.prototype = {
        _current: null,
        hasNext: function() {
            return !!this._next
        },
        next: function() {
            var a, b, c = this._current = this._next;
            if (this._current)
                if (a = c.firstChild)
                    this._next = a;
                else {
                    for (b = null; c !== this.root && !(b = c.nextSibling); )
                        c = c.parentNode;
                    this._next = b
                }
            return this._current
        },
        detach: function() {
            this._current = this._next = this.root = null
        }
    },
    x.prototype = {
        equals: function(a) {
            return this.node === a.node & this.offset == a.offset
        },
        inspect: function() {
            return "[DomPosition(" + u(this.node) + ":" + this.offset + ")]"
        }
    },
    y.prototype = {
        INDEX_SIZE_ERR: 1,
        HIERARCHY_REQUEST_ERR: 3,
        WRONG_DOCUMENT_ERR: 4,
        NO_MODIFICATION_ALLOWED_ERR: 7,
        NOT_FOUND_ERR: 8,
        NOT_SUPPORTED_ERR: 9,
        INVALID_STATE_ERR: 11
    },
    y.prototype.toString = function() {
        return this.message
    }
    ,
    a.dom = {
        arrayContains: D,
        isHtmlNamespace: c,
        parentElement: d,
        getNodeIndex: e,
        getNodeLength: f,
        getCommonAncestor: g,
        isAncestorOf: h,
        getClosestAncestorIn: i,
        isCharacterDataNode: j,
        insertAfter: k,
        splitDataNode: l,
        getDocument: m,
        getWindow: n,
        getIframeWindow: p,
        getIframeDocument: o,
        getBody: q,
        getRootContainer: r,
        comparePoints: s,
        inspectNode: u,
        fragmentFromNodeChildren: t,
        createIterator: w,
        DomPosition: x
    },
    a.DOMException = y
}),
rangy.createModule("DomRange", function(a) {
    function b(a, b) {
        return 3 != a.nodeType && (L.isAncestorOf(a, b.startContainer, !0) || L.isAncestorOf(a, b.endContainer, !0))
    }
    function c(a) {
        return L.getDocument(a.startContainer)
    }
    function d(a, b, c) {
        var d = a._listeners[b];
        if (d)
            for (var e = 0, f = d.length; f > e; ++e)
                d[e].call(a, {
                    target: a,
                    args: c
                })
    }
    function e(a) {
        return new M(a.parentNode,L.getNodeIndex(a))
    }
    function f(a) {
        return new M(a.parentNode,L.getNodeIndex(a) + 1)
    }
    function g(a, b, c) {
        var d = 11 == a.nodeType ? a.firstChild : a;
        return L.isCharacterDataNode(b) ? c == b.length ? L.insertAfter(a, b) : b.parentNode.insertBefore(a, 0 == c ? b : L.splitDataNode(b, c)) : c >= b.childNodes.length ? b.appendChild(a) : b.insertBefore(a, b.childNodes[c]),
        d
    }
    function h(a) {
        for (var b, d, e, f = c(a.range).createDocumentFragment(); d = a.next(); ) {
            if (b = a.isPartiallySelectedSubtree(),
            d = d.cloneNode(!b),
            b && (e = a.getSubtreeIterator(),
            d.appendChild(h(e)),
            e.detach(!0)),
            10 == d.nodeType)
                throw new N("HIERARCHY_REQUEST_ERR");
            f.appendChild(d)
        }
        return f
    }
    function i(a, b, c) {
        var d, e;
        c = c || {
            stop: !1
        };
        for (var f, g; f = a.next(); )
            if (a.isPartiallySelectedSubtree()) {
                if (b(f) === !1)
                    return c.stop = !0,
                    void 0;
                if (g = a.getSubtreeIterator(),
                i(g, b, c),
                g.detach(!0),
                c.stop)
                    return
            } else
                for (d = L.createIterator(f); e = d.next(); )
                    if (b(e) === !1)
                        return c.stop = !0,
                        void 0
    }
    function j(a) {
        for (var b; a.next(); )
            a.isPartiallySelectedSubtree() ? (b = a.getSubtreeIterator(),
            j(b),
            b.detach(!0)) : a.remove()
    }
    function k(a) {
        for (var b, d, e = c(a.range).createDocumentFragment(); b = a.next(); ) {
            if (a.isPartiallySelectedSubtree() ? (b = b.cloneNode(!1),
            d = a.getSubtreeIterator(),
            b.appendChild(k(d)),
            d.detach(!0)) : a.remove(),
            10 == b.nodeType)
                throw new N("HIERARCHY_REQUEST_ERR");
            e.appendChild(b)
        }
        return e
    }
    function l(a, b, c) {
        var d, e = !(!b || !b.length), f = !!c;
        e && (d = new RegExp("^(" + b.join("|") + ")$"));
        var g = [];
        return i(new n(a,!1), function(a) {
            e && !d.test(a.nodeType) || f && !c(a) || g.push(a)
        }),
        g
    }
    function m(a) {
        var b = "undefined" == typeof a.getName ? "Range" : a.getName();
        return "[" + b + "(" + L.inspectNode(a.startContainer) + ":" + a.startOffset + ", " + L.inspectNode(a.endContainer) + ":" + a.endOffset + ")]"
    }
    function n(a, b) {
        if (this.range = a,
        this.clonePartiallySelectedTextNodes = b,
        !a.collapsed) {
            this.sc = a.startContainer,
            this.so = a.startOffset,
            this.ec = a.endContainer,
            this.eo = a.endOffset;
            var c = a.commonAncestorContainer;
            this.sc === this.ec && L.isCharacterDataNode(this.sc) ? (this.isSingleCharacterDataNode = !0,
            this._first = this._last = this._next = this.sc) : (this._first = this._next = this.sc !== c || L.isCharacterDataNode(this.sc) ? L.getClosestAncestorIn(this.sc, c, !0) : this.sc.childNodes[this.so],
            this._last = this.ec !== c || L.isCharacterDataNode(this.ec) ? L.getClosestAncestorIn(this.ec, c, !0) : this.ec.childNodes[this.eo - 1])
        }
    }
    function o(a) {
        this.code = this[a],
        this.codeName = a,
        this.message = "RangeException: " + this.codeName
    }
    function p(a, b, c) {
        this.nodes = l(a, b, c),
        this._next = this.nodes[0],
        this._position = 0
    }
    function q(a) {
        return function(b, c) {
            for (var d, e = c ? b : b.parentNode; e; ) {
                if (d = e.nodeType,
                L.arrayContains(a, d))
                    return e;
                e = e.parentNode
            }
            return null
        }
    }
    function r(a, b) {
        if (W(a, b))
            throw new o("INVALID_NODE_TYPE_ERR")
    }
    function s(a) {
        if (!a.startContainer)
            throw new N("INVALID_STATE_ERR")
    }
    function t(a, b) {
        if (!L.arrayContains(b, a.nodeType))
            throw new o("INVALID_NODE_TYPE_ERR")
    }
    function u(a, b) {
        if (0 > b || b > (L.isCharacterDataNode(a) ? a.length : a.childNodes.length))
            throw new N("INDEX_SIZE_ERR")
    }
    function v(a, b) {
        if (U(a, !0) !== U(b, !0))
            throw new N("WRONG_DOCUMENT_ERR")
    }
    function w(a) {
        if (V(a, !0))
            throw new N("NO_MODIFICATION_ALLOWED_ERR")
    }
    function x(a, b) {
        if (!a)
            throw new N(b)
    }
    function y(a) {
        return !L.arrayContains(P, a.nodeType) && !U(a, !0)
    }
    function z(a, b) {
        return b <= (L.isCharacterDataNode(a) ? a.length : a.childNodes.length)
    }
    function A(a) {
        return !!a.startContainer && !!a.endContainer && !y(a.startContainer) && !y(a.endContainer) && z(a.startContainer, a.startOffset) && z(a.endContainer, a.endOffset)
    }
    function B(a) {
        if (s(a),
        !A(a))
            throw new Error("Range error: Range is no longer valid after DOM mutation (" + a.inspect() + ")")
    }
    function C() {}
    function D(a) {
        a.START_TO_START = ab,
        a.START_TO_END = bb,
        a.END_TO_END = cb,
        a.END_TO_START = db,
        a.NODE_BEFORE = eb,
        a.NODE_AFTER = fb,
        a.NODE_BEFORE_AND_AFTER = gb,
        a.NODE_INSIDE = hb
    }
    function E(a) {
        D(a),
        D(a.prototype)
    }
    function F(a, b) {
        return function() {
            B(this);
            var c, d, e = this.startContainer, g = this.startOffset, h = this.commonAncestorContainer, j = new n(this,!0);
            e !== h && (c = L.getClosestAncestorIn(e, h, !0),
            d = f(c),
            e = d.node,
            g = d.offset),
            i(j, w),
            j.reset();
            var k = a(j);
            return j.detach(),
            b(this, e, g, e, g),
            k
        }
    }
    function G(c, d, g) {
        function h(a, b) {
            return function(c) {
                s(this),
                t(c, O),
                t(T(c), P);
                var d = (a ? e : f)(c);
                (b ? i : l)(this, d.node, d.offset)
            }
        }
        function i(a, b, c) {
            var e = a.endContainer
              , f = a.endOffset;
            (b !== a.startContainer || c !== a.startOffset) && ((T(b) != T(e) || 1 == L.comparePoints(b, c, e, f)) && (e = b,
            f = c),
            d(a, b, c, e, f))
        }
        function l(a, b, c) {
            var e = a.startContainer
              , f = a.startOffset;
            (b !== a.endContainer || c !== a.endOffset) && ((T(b) != T(e) || -1 == L.comparePoints(b, c, e, f)) && (e = b,
            f = c),
            d(a, e, f, b, c))
        }
        function m(a, b, c) {
            (b !== a.startContainer || c !== a.startOffset || b !== a.endContainer || c !== a.endOffset) && d(a, b, c, b, c)
        }
        c.prototype = new C,
        a.util.extend(c.prototype, {
            setStart: function(a, b) {
                s(this),
                r(a, !0),
                u(a, b),
                i(this, a, b)
            },
            setEnd: function(a, b) {
                s(this),
                r(a, !0),
                u(a, b),
                l(this, a, b)
            },
            setStartBefore: h(!0, !0),
            setStartAfter: h(!1, !0),
            setEndBefore: h(!0, !1),
            setEndAfter: h(!1, !1),
            collapse: function(a) {
                B(this),
                a ? d(this, this.startContainer, this.startOffset, this.startContainer, this.startOffset) : d(this, this.endContainer, this.endOffset, this.endContainer, this.endOffset)
            },
            selectNodeContents: function(a) {
                s(this),
                r(a, !0),
                d(this, a, 0, a, L.getNodeLength(a))
            },
            selectNode: function(a) {
                s(this),
                r(a, !1),
                t(a, O);
                var b = e(a)
                  , c = f(a);
                d(this, b.node, b.offset, c.node, c.offset)
            },
            extractContents: F(k, d),
            deleteContents: F(j, d),
            canSurroundContents: function() {
                B(this),
                w(this.startContainer),
                w(this.endContainer);
                var a = new n(this,!0)
                  , c = a._first && b(a._first, this) || a._last && b(a._last, this);
                return a.detach(),
                !c
            },
            detach: function() {
                g(this)
            },
            splitBoundaries: function() {
                B(this);
                var a = this.startContainer
                  , b = this.startOffset
                  , c = this.endContainer
                  , e = this.endOffset
                  , f = a === c;
                L.isCharacterDataNode(c) && e > 0 && e < c.length && L.splitDataNode(c, e),
                L.isCharacterDataNode(a) && b > 0 && b < a.length && (a = L.splitDataNode(a, b),
                f ? (e -= b,
                c = a) : c == a.parentNode && e >= L.getNodeIndex(a) && e++,
                b = 0),
                d(this, a, b, c, e)
            },
            normalizeBoundaries: function() {
                B(this);
                var a = this.startContainer
                  , b = this.startOffset
                  , c = this.endContainer
                  , e = this.endOffset
                  , f = function(a) {
                    var b = a.nextSibling;
                    b && b.nodeType == a.nodeType && (c = a,
                    e = a.length,
                    a.appendData(b.data),
                    b.parentNode.removeChild(b))
                }
                  , g = function(d) {
                    var f = d.previousSibling;
                    if (f && f.nodeType == d.nodeType) {
                        a = d;
                        var g = d.length;
                        if (b = f.length,
                        d.insertData(0, f.data),
                        f.parentNode.removeChild(f),
                        a == c)
                            e += b,
                            c = a;
                        else if (c == d.parentNode) {
                            var h = L.getNodeIndex(d);
                            e == h ? (c = d,
                            e = g) : e > h && e--
                        }
                    }
                }
                  , h = !0;
                if (L.isCharacterDataNode(c))
                    c.length == e && f(c);
                else {
                    if (e > 0) {
                        var i = c.childNodes[e - 1];
                        i && L.isCharacterDataNode(i) && f(i)
                    }
                    h = !this.collapsed
                }
                if (h) {
                    if (L.isCharacterDataNode(a))
                        0 == b && g(a);
                    else if (b < a.childNodes.length) {
                        var j = a.childNodes[b];
                        j && L.isCharacterDataNode(j) && g(j)
                    }
                } else
                    a = c,
                    b = e;
                d(this, a, b, c, e)
            },
            collapseToPoint: function(a, b) {
                s(this),
                r(a, !0),
                u(a, b),
                m(this, a, b)
            }
        }),
        E(c)
    }
    function H(a) {
        a.collapsed = a.startContainer === a.endContainer && a.startOffset === a.endOffset,
        a.commonAncestorContainer = a.collapsed ? a.startContainer : L.getCommonAncestor(a.startContainer, a.endContainer)
    }
    function I(a, b, c, e, f) {
        var g = a.startContainer !== b || a.startOffset !== c
          , h = a.endContainer !== e || a.endOffset !== f;
        a.startContainer = b,
        a.startOffset = c,
        a.endContainer = e,
        a.endOffset = f,
        H(a),
        d(a, "boundarychange", {
            startMoved: g,
            endMoved: h
        })
    }
    function J(a) {
        s(a),
        a.startContainer = a.startOffset = a.endContainer = a.endOffset = null,
        a.collapsed = a.commonAncestorContainer = null,
        d(a, "detach", null),
        a._listeners = null
    }
    function K(a) {
        this.startContainer = a,
        this.startOffset = 0,
        this.endContainer = a,
        this.endOffset = 0,
        this._listeners = {
            boundarychange: [],
            detach: []
        },
        H(this)
    }
    a.requireModules(["DomUtil"]);
    var L = a.dom
      , M = L.DomPosition
      , N = a.DOMException;
    n.prototype = {
        _current: null,
        _next: null,
        _first: null,
        _last: null,
        isSingleCharacterDataNode: !1,
        reset: function() {
            this._current = null,
            this._next = this._first
        },
        hasNext: function() {
            return !!this._next
        },
        next: function() {
            var a = this._current = this._next;
            return a && (this._next = a !== this._last ? a.nextSibling : null,
            L.isCharacterDataNode(a) && this.clonePartiallySelectedTextNodes && (a === this.ec && (a = a.cloneNode(!0)).deleteData(this.eo, a.length - this.eo),
            this._current === this.sc && (a = a.cloneNode(!0)).deleteData(0, this.so))),
            a
        },
        remove: function() {
            var a, b, c = this._current;
            !L.isCharacterDataNode(c) || c !== this.sc && c !== this.ec ? c.parentNode && c.parentNode.removeChild(c) : (a = c === this.sc ? this.so : 0,
            b = c === this.ec ? this.eo : c.length,
            a != b && c.deleteData(a, b - a))
        },
        isPartiallySelectedSubtree: function() {
            var a = this._current;
            return b(a, this.range)
        },
        getSubtreeIterator: function() {
            var a;
            if (this.isSingleCharacterDataNode)
                a = this.range.cloneRange(),
                a.collapse();
            else {
                a = new K(c(this.range));
                var b = this._current
                  , d = b
                  , e = 0
                  , f = b
                  , g = L.getNodeLength(b);
                L.isAncestorOf(b, this.sc, !0) && (d = this.sc,
                e = this.so),
                L.isAncestorOf(b, this.ec, !0) && (f = this.ec,
                g = this.eo),
                I(a, d, e, f, g)
            }
            return new n(a,this.clonePartiallySelectedTextNodes)
        },
        detach: function(a) {
            a && this.range.detach(),
            this.range = this._current = this._next = this._first = this._last = this.sc = this.so = this.ec = this.eo = null
        }
    },
    o.prototype = {
        BAD_BOUNDARYPOINTS_ERR: 1,
        INVALID_NODE_TYPE_ERR: 2
    },
    o.prototype.toString = function() {
        return this.message
    }
    ,
    p.prototype = {
        _current: null,
        hasNext: function() {
            return !!this._next
        },
        next: function() {
            return this._current = this._next,
            this._next = this.nodes[++this._position],
            this._current
        },
        detach: function() {
            this._current = this._next = this.nodes = null
        }
    };
    var O = [1, 3, 4, 5, 7, 8, 10]
      , P = [2, 9, 11]
      , Q = [5, 6, 10, 12]
      , R = [1, 3, 4, 5, 7, 8, 10, 11]
      , S = [1, 3, 4, 5, 7, 8]
      , T = L.getRootContainer
      , U = q([9, 11])
      , V = q(Q)
      , W = q([6, 10, 12])
      , X = document.createElement("style")
      , Y = !1;
    try {
        X.innerHTML = "<b>x</b>",
        Y = 3 == X.firstChild.nodeType
    } catch (Z) {}
    a.features.htmlParsingConforms = Y;
    var $ = Y ? function(a) {
        var b = this.startContainer
          , c = L.getDocument(b);
        if (!b)
            throw new N("INVALID_STATE_ERR");
        var d = null;
        return 1 == b.nodeType ? d = b : L.isCharacterDataNode(b) && (d = L.parentElement(b)),
        d = null === d || "HTML" == d.nodeName && L.isHtmlNamespace(L.getDocument(d).documentElement) && L.isHtmlNamespace(d) ? c.createElement("body") : d.cloneNode(!1),
        d.innerHTML = a,
        L.fragmentFromNodeChildren(d)
    }
    : function(a) {
        s(this);
        var b = c(this)
          , d = b.createElement("body");
        return d.innerHTML = a,
        L.fragmentFromNodeChildren(d)
    }
      , _ = ["startContainer", "startOffset", "endContainer", "endOffset", "collapsed", "commonAncestorContainer"]
      , ab = 0
      , bb = 1
      , cb = 2
      , db = 3
      , eb = 0
      , fb = 1
      , gb = 2
      , hb = 3;
    C.prototype = {
        attachListener: function(a, b) {
            this._listeners[a].push(b)
        },
        compareBoundaryPoints: function(a, b) {
            B(this),
            v(this.startContainer, b.startContainer);
            var c, d, e, f, g = a == db || a == ab ? "start" : "end", h = a == bb || a == ab ? "start" : "end";
            return c = this[g + "Container"],
            d = this[g + "Offset"],
            e = b[h + "Container"],
            f = b[h + "Offset"],
            L.comparePoints(c, d, e, f)
        },
        insertNode: function(a) {
            if (B(this),
            t(a, R),
            w(this.startContainer),
            L.isAncestorOf(a, this.startContainer, !0))
                throw new N("HIERARCHY_REQUEST_ERR");
            var b = g(a, this.startContainer, this.startOffset);
            this.setStartBefore(b)
        },
        cloneContents: function() {
            B(this);
            var a, b;
            if (this.collapsed)
                return c(this).createDocumentFragment();
            if (this.startContainer === this.endContainer && L.isCharacterDataNode(this.startContainer))
                return a = this.startContainer.cloneNode(!0),
                a.data = a.data.slice(this.startOffset, this.endOffset),
                b = c(this).createDocumentFragment(),
                b.appendChild(a),
                b;
            var d = new n(this,!0);
            return a = h(d),
            d.detach(),
            a
        },
        canSurroundContents: function() {
            B(this),
            w(this.startContainer),
            w(this.endContainer);
            var a = new n(this,!0)
              , c = a._first && b(a._first, this) || a._last && b(a._last, this);
            return a.detach(),
            !c
        },
        surroundContents: function(a) {
            if (t(a, S),
            !this.canSurroundContents())
                throw new o("BAD_BOUNDARYPOINTS_ERR");
            var b = this.extractContents();
            if (a.hasChildNodes())
                for (; a.lastChild; )
                    a.removeChild(a.lastChild);
            g(a, this.startContainer, this.startOffset),
            a.appendChild(b),
            this.selectNode(a)
        },
        cloneRange: function() {
            B(this);
            for (var a, b = new K(c(this)), d = _.length; d--; )
                a = _[d],
                b[a] = this[a];
            return b
        },
        toString: function() {
            B(this);
            var a = this.startContainer;
            if (a === this.endContainer && L.isCharacterDataNode(a))
                return 3 == a.nodeType || 4 == a.nodeType ? a.data.slice(this.startOffset, this.endOffset) : "";
            var b = []
              , c = new n(this,!0);
            return i(c, function(a) {
                (3 == a.nodeType || 4 == a.nodeType) && b.push(a.data)
            }),
            c.detach(),
            b.join("")
        },
        compareNode: function(a) {
            B(this);
            var b = a.parentNode
              , c = L.getNodeIndex(a);
            if (!b)
                throw new N("NOT_FOUND_ERR");
            var d = this.comparePoint(b, c)
              , e = this.comparePoint(b, c + 1);
            return 0 > d ? e > 0 ? gb : eb : e > 0 ? fb : hb
        },
        comparePoint: function(a, b) {
            return B(this),
            x(a, "HIERARCHY_REQUEST_ERR"),
            v(a, this.startContainer),
            L.comparePoints(a, b, this.startContainer, this.startOffset) < 0 ? -1 : L.comparePoints(a, b, this.endContainer, this.endOffset) > 0 ? 1 : 0
        },
        createContextualFragment: $,
        toHtml: function() {
            B(this);
            var a = c(this).createElement("div");
            return a.appendChild(this.cloneContents()),
            a.innerHTML
        },
        intersectsNode: function(a, b) {
            if (B(this),
            x(a, "NOT_FOUND_ERR"),
            L.getDocument(a) !== c(this))
                return !1;
            var d = a.parentNode
              , e = L.getNodeIndex(a);
            x(d, "NOT_FOUND_ERR");
            var f = L.comparePoints(d, e, this.endContainer, this.endOffset)
              , g = L.comparePoints(d, e + 1, this.startContainer, this.startOffset);
            return b ? 0 >= f && g >= 0 : 0 > f && g > 0
        },
        isPointInRange: function(a, b) {
            return B(this),
            x(a, "HIERARCHY_REQUEST_ERR"),
            v(a, this.startContainer),
            L.comparePoints(a, b, this.startContainer, this.startOffset) >= 0 && L.comparePoints(a, b, this.endContainer, this.endOffset) <= 0
        },
        intersectsRange: function(a, b) {
            if (B(this),
            c(a) != c(this))
                throw new N("WRONG_DOCUMENT_ERR");
            var d = L.comparePoints(this.startContainer, this.startOffset, a.endContainer, a.endOffset)
              , e = L.comparePoints(this.endContainer, this.endOffset, a.startContainer, a.startOffset);
            return b ? 0 >= d && e >= 0 : 0 > d && e > 0
        },
        intersection: function(a) {
            if (this.intersectsRange(a)) {
                var b = L.comparePoints(this.startContainer, this.startOffset, a.startContainer, a.startOffset)
                  , c = L.comparePoints(this.endContainer, this.endOffset, a.endContainer, a.endOffset)
                  , d = this.cloneRange();
                return -1 == b && d.setStart(a.startContainer, a.startOffset),
                1 == c && d.setEnd(a.endContainer, a.endOffset),
                d
            }
            return null
        },
        union: function(a) {
            if (this.intersectsRange(a, !0)) {
                var b = this.cloneRange();
                return -1 == L.comparePoints(a.startContainer, a.startOffset, this.startContainer, this.startOffset) && b.setStart(a.startContainer, a.startOffset),
                1 == L.comparePoints(a.endContainer, a.endOffset, this.endContainer, this.endOffset) && b.setEnd(a.endContainer, a.endOffset),
                b
            }
            throw new o("Ranges do not intersect")
        },
        containsNode: function(a, b) {
            return b ? this.intersectsNode(a, !1) : this.compareNode(a) == hb
        },
        containsNodeContents: function(a) {
            return this.comparePoint(a, 0) >= 0 && this.comparePoint(a, L.getNodeLength(a)) <= 0
        },
        containsRange: function(a) {
            return this.intersection(a).equals(a)
        },
        containsNodeText: function(a) {
            var b = this.cloneRange();
            b.selectNode(a);
            var c = b.getNodes([3]);
            if (c.length > 0) {
                b.setStart(c[0], 0);
                var d = c.pop();
                b.setEnd(d, d.length);
                var e = this.containsRange(b);
                return b.detach(),
                e
            }
            return this.containsNodeContents(a)
        },
        createNodeIterator: function(a, b) {
            return B(this),
            new p(this,a,b)
        },
        getNodes: function(a, b) {
            return B(this),
            l(this, a, b)
        },
        getDocument: function() {
            return c(this)
        },
        collapseBefore: function(a) {
            s(this),
            this.setEndBefore(a),
            this.collapse(!1)
        },
        collapseAfter: function(a) {
            s(this),
            this.setStartAfter(a),
            this.collapse(!0)
        },
        getName: function() {
            return "DomRange"
        },
        equals: function(a) {
            return K.rangesEqual(this, a)
        },
        isValid: function() {
            return A(this)
        },
        inspect: function() {
            return m(this)
        }
    },
    G(K, I, J),
    a.rangePrototype = C.prototype,
    K.rangeProperties = _,
    K.RangeIterator = n,
    K.copyComparisonConstants = E,
    K.createPrototypeRange = G,
    K.inspect = m,
    K.getRangeDocument = c,
    K.rangesEqual = function(a, b) {
        return a.startContainer === b.startContainer && a.startOffset === b.startOffset && a.endContainer === b.endContainer && a.endOffset === b.endOffset
    }
    ,
    a.DomRange = K,
    a.RangeException = o
}),
rangy.createModule("WrappedRange", function(a) {
    function b(a) {
        var b = a.parentElement()
          , c = a.duplicate();
        c.collapse(!0);
        var d = c.parentElement();
        c = a.duplicate(),
        c.collapse(!1);
        var e = c.parentElement()
          , f = d == e ? d : g.getCommonAncestor(d, e);
        return f == b ? f : g.getCommonAncestor(b, f)
    }
    function c(a) {
        return 0 == a.compareEndPoints("StartToEnd", a)
    }
    function d(a, b, c, d) {
        var e = a.duplicate();
        e.collapse(c);
        var f = e.parentElement();
        if (g.isAncestorOf(b, f, !0) || (f = b),
        !f.canHaveHTML)
            return new h(f.parentNode,g.getNodeIndex(f));
        var i, j, k, l, m, n = g.getDocument(f).createElement("span"), o = c ? "StartToStart" : "StartToEnd";
        do
            f.insertBefore(n, n.previousSibling),
            e.moveToElementText(n);
        while ((i = e.compareEndPoints(o, a)) > 0 && n.previousSibling);
        if (m = n.nextSibling,
        -1 == i && m && g.isCharacterDataNode(m)) {
            e.setEndPoint(c ? "EndToStart" : "EndToEnd", a);
            var p;
            if (/[\r\n]/.test(m.data)) {
                var q = e.duplicate()
                  , r = q.text.replace(/\r\n/g, "\r").length;
                for (p = q.moveStart("character", r); -1 == (i = q.compareEndPoints("StartToEnd", q)); )
                    p++,
                    q.moveStart("character", 1)
            } else
                p = e.text.length;
            l = new h(m,p)
        } else
            j = (d || !c) && n.previousSibling,
            k = (d || c) && n.nextSibling,
            l = k && g.isCharacterDataNode(k) ? new h(k,0) : j && g.isCharacterDataNode(j) ? new h(j,j.length) : new h(f,g.getNodeIndex(n));
        return n.parentNode.removeChild(n),
        l
    }
    function e(a, b) {
        var c, d, e, f, h = a.offset, i = g.getDocument(a.node), j = i.body.createTextRange(), k = g.isCharacterDataNode(a.node);
        return k ? (c = a.node,
        d = c.parentNode) : (f = a.node.childNodes,
        c = h < f.length ? f[h] : null,
        d = a.node),
        e = i.createElement("span"),
        e.innerHTML = "&#feff;",
        c ? d.insertBefore(e, c) : d.appendChild(e),
        j.moveToElementText(e),
        j.collapse(!b),
        d.removeChild(e),
        k && j[b ? "moveStart" : "moveEnd"]("character", h),
        j
    }
    a.requireModules(["DomUtil", "DomRange"]);
    var f, g = a.dom, h = g.DomPosition, i = a.DomRange;
    if (!a.features.implementsDomRange || a.features.implementsTextRange && a.config.preferTextRange) {
        if (a.features.implementsTextRange) {
            f = function(a) {
                this.textRange = a,
                this.refresh()
            }
            ,
            f.prototype = new i(document),
            f.prototype.refresh = function() {
                var a, e, f = b(this.textRange);
                c(this.textRange) ? e = a = d(this.textRange, f, !0, !0) : (a = d(this.textRange, f, !0, !1),
                e = d(this.textRange, f, !1, !1)),
                this.setStart(a.node, a.offset),
                this.setEnd(e.node, e.offset)
            }
            ,
            i.copyComparisonConstants(f);
            var j = function() {
                return this
            }();
            "undefined" == typeof j.Range && (j.Range = f),
            a.createNativeRange = function(a) {
                return a = a || document,
                a.body.createTextRange()
            }
        }
    } else
        !function() {
            function b(a) {
                for (var b, c = k.length; c--; )
                    b = k[c],
                    a[b] = a.nativeRange[b]
            }
            function c(a, b, c, d, e) {
                var f = a.startContainer !== b || a.startOffset != c
                  , g = a.endContainer !== d || a.endOffset != e;
                (f || g) && (a.setEnd(d, e),
                a.setStart(b, c))
            }
            function d(a) {
                a.nativeRange.detach(),
                a.detached = !0;
                for (var b, c = k.length; c--; )
                    b = k[c],
                    a[b] = null
            }
            var e, h, j, k = i.rangeProperties;
            f = function(a) {
                if (!a)
                    throw new Error("Range must be specified");
                this.nativeRange = a,
                b(this)
            }
            ,
            i.createPrototypeRange(f, c, d),
            e = f.prototype,
            e.selectNode = function(a) {
                this.nativeRange.selectNode(a),
                b(this)
            }
            ,
            e.deleteContents = function() {
                this.nativeRange.deleteContents(),
                b(this)
            }
            ,
            e.extractContents = function() {
                var a = this.nativeRange.extractContents();
                return b(this),
                a
            }
            ,
            e.cloneContents = function() {
                return this.nativeRange.cloneContents()
            }
            ,
            e.surroundContents = function(a) {
                this.nativeRange.surroundContents(a),
                b(this)
            }
            ,
            e.collapse = function(a) {
                this.nativeRange.collapse(a),
                b(this)
            }
            ,
            e.cloneRange = function() {
                return new f(this.nativeRange.cloneRange())
            }
            ,
            e.refresh = function() {
                b(this)
            }
            ,
            e.toString = function() {
                return this.nativeRange.toString()
            }
            ;
            var l = document.createTextNode("test");
            g.getBody(document).appendChild(l);
            var m = document.createRange();
            m.setStart(l, 0),
            m.setEnd(l, 0);
            try {
                m.setStart(l, 1),
                h = !0,
                e.setStart = function(a, c) {
                    this.nativeRange.setStart(a, c),
                    b(this)
                }
                ,
                e.setEnd = function(a, c) {
                    this.nativeRange.setEnd(a, c),
                    b(this)
                }
                ,
                j = function(a) {
                    return function(c) {
                        this.nativeRange[a](c),
                        b(this)
                    }
                }
            } catch (n) {
                h = !1,
                e.setStart = function(a, c) {
                    try {
                        this.nativeRange.setStart(a, c)
                    } catch (d) {
                        this.nativeRange.setEnd(a, c),
                        this.nativeRange.setStart(a, c)
                    }
                    b(this)
                }
                ,
                e.setEnd = function(a, c) {
                    try {
                        this.nativeRange.setEnd(a, c)
                    } catch (d) {
                        this.nativeRange.setStart(a, c),
                        this.nativeRange.setEnd(a, c)
                    }
                    b(this)
                }
                ,
                j = function(a, c) {
                    return function(d) {
                        try {
                            this.nativeRange[a](d)
                        } catch (e) {
                            this.nativeRange[c](d),
                            this.nativeRange[a](d)
                        }
                        b(this)
                    }
                }
            }
            e.setStartBefore = j("setStartBefore", "setEndBefore"),
            e.setStartAfter = j("setStartAfter", "setEndAfter"),
            e.setEndBefore = j("setEndBefore", "setStartBefore"),
            e.setEndAfter = j("setEndAfter", "setStartAfter"),
            m.selectNodeContents(l),
            e.selectNodeContents = m.startContainer == l && m.endContainer == l && 0 == m.startOffset && m.endOffset == l.length ? function(a) {
                this.nativeRange.selectNodeContents(a),
                b(this)
            }
            : function(a) {
                this.setStart(a, 0),
                this.setEnd(a, i.getEndOffset(a))
            }
            ,
            m.selectNodeContents(l),
            m.setEnd(l, 3);
            var o = document.createRange();
            o.selectNodeContents(l),
            o.setEnd(l, 4),
            o.setStart(l, 2),
            e.compareBoundaryPoints = -1 == m.compareBoundaryPoints(m.START_TO_END, o) & 1 == m.compareBoundaryPoints(m.END_TO_START, o) ? function(a, b) {
                return b = b.nativeRange || b,
                a == b.START_TO_END ? a = b.END_TO_START : a == b.END_TO_START && (a = b.START_TO_END),
                this.nativeRange.compareBoundaryPoints(a, b)
            }
            : function(a, b) {
                return this.nativeRange.compareBoundaryPoints(a, b.nativeRange || b)
            }
            ,
            a.util.isHostMethod(m, "createContextualFragment") && (e.createContextualFragment = function(a) {
                return this.nativeRange.createContextualFragment(a)
            }
            ),
            g.getBody(document).removeChild(l),
            m.detach(),
            o.detach()
        }(),
        a.createNativeRange = function(a) {
            return a = a || document,
            a.createRange()
        }
        ;
    a.features.implementsTextRange && (f.rangeToTextRange = function(a) {
        if (a.collapsed) {
            var b = e(new h(a.startContainer,a.startOffset), !0);
            return b
        }
        var c = e(new h(a.startContainer,a.startOffset), !0)
          , d = e(new h(a.endContainer,a.endOffset), !1)
          , f = g.getDocument(a.startContainer).body.createTextRange();
        return f.setEndPoint("StartToStart", c),
        f.setEndPoint("EndToEnd", d),
        f
    }
    ),
    f.prototype.getName = function() {
        return "WrappedRange"
    }
    ,
    a.WrappedRange = f,
    a.createRange = function(b) {
        return b = b || document,
        new f(a.createNativeRange(b))
    }
    ,
    a.createRangyRange = function(a) {
        return a = a || document,
        new i(a)
    }
    ,
    a.createIframeRange = function(b) {
        return a.createRange(g.getIframeDocument(b))
    }
    ,
    a.createIframeRangyRange = function(b) {
        return a.createRangyRange(g.getIframeDocument(b))
    }
    ,
    a.addCreateMissingNativeApiListener(function(b) {
        var c = b.document;
        "undefined" == typeof c.createRange && (c.createRange = function() {
            return a.createRange(this)
        }
        ),
        c = b = null
    })
}),
rangy.createModule("WrappedSelection", function(a, b) {
    function c(a) {
        return (a || window).getSelection()
    }
    function d(a) {
        return (a || window).document.selection
    }
    function e(a, b, c) {
        var d = c ? "end" : "start"
          , e = c ? "start" : "end";
        a.anchorNode = b[d + "Container"],
        a.anchorOffset = b[d + "Offset"],
        a.focusNode = b[e + "Container"],
        a.focusOffset = b[e + "Offset"]
    }
    function f(a) {
        var b = a.nativeSelection;
        a.anchorNode = b.anchorNode,
        a.anchorOffset = b.anchorOffset,
        a.focusNode = b.focusNode,
        a.focusOffset = b.focusOffset
    }
    function g(a) {
        a.anchorNode = a.focusNode = null,
        a.anchorOffset = a.focusOffset = 0,
        a.rangeCount = 0,
        a.isCollapsed = !0,
        a._ranges.length = 0
    }
    function h(b) {
        var c;
        return b instanceof y ? (c = b._selectionNativeRange,
        c || (c = a.createNativeRange(w.getDocument(b.startContainer)),
        c.setEnd(b.endContainer, b.endOffset),
        c.setStart(b.startContainer, b.startOffset),
        b._selectionNativeRange = c,
        b.attachListener("detach", function() {
            this._selectionNativeRange = null
        }))) : b instanceof z ? c = b.nativeRange : a.features.implementsDomRange && b instanceof w.getWindow(b.startContainer).Range && (c = b),
        c
    }
    function i(a) {
        if (!a.length || 1 != a[0].nodeType)
            return !1;
        for (var b = 1, c = a.length; c > b; ++b)
            if (!w.isAncestorOf(a[0], a[b]))
                return !1;
        return !0
    }
    function j(a) {
        var b = a.getNodes();
        if (!i(b))
            throw new Error("getSingleElementFromRange: range " + a.inspect() + " did not consist of a single element");
        return b[0]
    }
    function k(a) {
        return !!a && "undefined" != typeof a.text
    }
    function l(a, b) {
        var c = new z(b);
        a._ranges = [c],
        e(a, c, !1),
        a.rangeCount = 1,
        a.isCollapsed = c.collapsed
    }
    function m(b) {
        if (b._ranges.length = 0,
        "None" == b.docSelection.type)
            g(b);
        else {
            var c = b.docSelection.createRange();
            if (k(c))
                l(b, c);
            else {
                b.rangeCount = c.length;
                for (var d, f = w.getDocument(c.item(0)), h = 0; h < b.rangeCount; ++h)
                    d = a.createRange(f),
                    d.selectNode(c.item(h)),
                    b._ranges.push(d);
                b.isCollapsed = 1 == b.rangeCount && b._ranges[0].collapsed,
                e(b, b._ranges[b.rangeCount - 1], !1)
            }
        }
    }
    function n(a, b) {
        for (var c = a.docSelection.createRange(), d = j(b), e = w.getDocument(c.item(0)), f = w.getBody(e).createControlRange(), g = 0, h = c.length; h > g; ++g)
            f.add(c.item(g));
        try {
            f.add(d)
        } catch (i) {
            throw new Error("addRange(): Element within the specified Range could not be added to control selection (does it have layout?)")
        }
        f.select(),
        m(a)
    }
    function o(a, b, c) {
        this.nativeSelection = a,
        this.docSelection = b,
        this._ranges = [],
        this.win = c,
        this.refresh()
    }
    function p(a, b) {
        for (var c, d = w.getDocument(b[0].startContainer), e = w.getBody(d).createControlRange(), f = 0; rangeCount > f; ++f) {
            c = j(b[f]);
            try {
                e.add(c)
            } catch (g) {
                throw new Error("setRanges(): Element within the one of the specified Ranges could not be added to control selection (does it have layout?)")
            }
        }
        e.select(),
        m(a)
    }
    function q(a, b) {
        if (a.anchorNode && w.getDocument(a.anchorNode) !== w.getDocument(b))
            throw new A("WRONG_DOCUMENT_ERR")
    }
    function r(a) {
        var b = []
          , c = new B(a.anchorNode,a.anchorOffset)
          , d = new B(a.focusNode,a.focusOffset)
          , e = "function" == typeof a.getName ? a.getName() : "Selection";
        if ("undefined" != typeof a.rangeCount)
            for (var f = 0, g = a.rangeCount; g > f; ++f)
                b[f] = y.inspect(a.getRangeAt(f));
        return "[" + e + "(Ranges: " + b.join(", ") + ")(anchor: " + c.inspect() + ", focus: " + d.inspect() + "]"
    }
    a.requireModules(["DomUtil", "DomRange", "WrappedRange"]),
    a.config.checkSelectionRanges = !0;
    var s, t, u = "boolean", v = "_rangySelection", w = a.dom, x = a.util, y = a.DomRange, z = a.WrappedRange, A = a.DOMException, B = w.DomPosition, C = "Control", D = a.util.isHostMethod(window, "getSelection"), E = a.util.isHostObject(document, "selection"), F = E && (!D || a.config.preferTextRange);
    F ? (s = d,
    a.isSelectionValid = function(a) {
        var b = (a || window).document
          , c = b.selection;
        return "None" != c.type || w.getDocument(c.createRange().parentElement()) == b
    }
    ) : D ? (s = c,
    a.isSelectionValid = function() {
        return !0
    }
    ) : b.fail("Neither document.selection or window.getSelection() detected."),
    a.getNativeSelection = s;
    var G = s()
      , H = a.createNativeRange(document)
      , I = w.getBody(document)
      , J = x.areHostObjects(G, ["anchorNode", "focusNode"] && x.areHostProperties(G, ["anchorOffset", "focusOffset"]));
    a.features.selectionHasAnchorAndFocus = J;
    var K = x.isHostMethod(G, "extend");
    a.features.selectionHasExtend = K;
    var L = "number" == typeof G.rangeCount;
    a.features.selectionHasRangeCount = L;
    var M = !1
      , N = !0;
    x.areHostMethods(G, ["addRange", "getRangeAt", "removeAllRanges"]) && "number" == typeof G.rangeCount && a.features.implementsDomRange && !function() {
        var a = document.createElement("iframe");
        a.frameBorder = 0,
        a.style.position = "absolute",
        a.style.left = "-10000px",
        I.appendChild(a);
        var b = w.getIframeDocument(a);
        b.open(),
        b.write("<html><head></head><body>12</body></html>"),
        b.close();
        var c = w.getIframeWindow(a).getSelection()
          , d = b.documentElement
          , e = d.lastChild
          , f = e.firstChild
          , g = b.createRange();
        g.setStart(f, 1),
        g.collapse(!0),
        c.addRange(g),
        N = 1 == c.rangeCount,
        c.removeAllRanges();
        var h = g.cloneRange();
        g.setStart(f, 0),
        h.setEnd(f, 2),
        c.addRange(g),
        c.addRange(h),
        M = 2 == c.rangeCount,
        g.detach(),
        h.detach(),
        I.removeChild(a)
    }(),
    a.features.selectionSupportsMultipleRanges = M,
    a.features.collapsedNonEditableSelectionsSupported = N;
    var O, P = !1;
    I && x.isHostMethod(I, "createControlRange") && (O = I.createControlRange(),
    x.areHostProperties(O, ["item", "add"]) && (P = !0)),
    a.features.implementsControlRange = P,
    t = J ? function(a) {
        return a.anchorNode === a.focusNode && a.anchorOffset === a.focusOffset
    }
    : function(a) {
        return a.rangeCount ? a.getRangeAt(a.rangeCount - 1).collapsed : !1
    }
    ;
    var Q;
    x.isHostMethod(G, "getRangeAt") ? Q = function(a, b) {
        try {
            return a.getRangeAt(b)
        } catch (c) {
            return null
        }
    }
    : J && (Q = function(b) {
        var c = w.getDocument(b.anchorNode)
          , d = a.createRange(c);
        return d.setStart(b.anchorNode, b.anchorOffset),
        d.setEnd(b.focusNode, b.focusOffset),
        d.collapsed !== this.isCollapsed && (d.setStart(b.focusNode, b.focusOffset),
        d.setEnd(b.anchorNode, b.anchorOffset)),
        d
    }
    ),
    a.getSelection = function(a) {
        a = a || window;
        var b = a[v]
          , c = s(a)
          , e = E ? d(a) : null;
        return b ? (b.nativeSelection = c,
        b.docSelection = e,
        b.refresh(a)) : (b = new o(c,e,a),
        a[v] = b),
        b
    }
    ,
    a.getIframeSelection = function(b) {
        return a.getSelection(w.getIframeWindow(b))
    }
    ;
    var R = o.prototype;
    if (!F && J && x.areHostMethods(G, ["removeAllRanges", "addRange"])) {
        R.removeAllRanges = function() {
            this.nativeSelection.removeAllRanges(),
            g(this)
        }
        ;
        var S = function(b, c) {
            var d = y.getRangeDocument(c)
              , e = a.createRange(d);
            e.collapseToPoint(c.endContainer, c.endOffset),
            b.nativeSelection.addRange(h(e)),
            b.nativeSelection.extend(c.startContainer, c.startOffset),
            b.refresh()
        };
        R.addRange = L ? function(b, c) {
            if (P && E && this.docSelection.type == C)
                n(this, b);
            else if (c && K)
                S(this, b);
            else {
                var d;
                if (M ? d = this.rangeCount : (this.removeAllRanges(),
                d = 0),
                this.nativeSelection.addRange(h(b)),
                this.rangeCount = this.nativeSelection.rangeCount,
                this.rangeCount == d + 1) {
                    if (a.config.checkSelectionRanges) {
                        var f = Q(this.nativeSelection, this.rangeCount - 1);
                        f && !y.rangesEqual(f, b) && (b = new z(f))
                    }
                    this._ranges[this.rangeCount - 1] = b,
                    e(this, b, V(this.nativeSelection)),
                    this.isCollapsed = t(this)
                } else
                    this.refresh()
            }
        }
        : function(a, b) {
            b && K ? S(this, a) : (this.nativeSelection.addRange(h(a)),
            this.refresh())
        }
        ,
        R.setRanges = function(a) {
            if (P && a.length > 1)
                p(this, a);
            else {
                this.removeAllRanges();
                for (var b = 0, c = a.length; c > b; ++b)
                    this.addRange(a[b])
            }
        }
    } else {
        if (!(x.isHostMethod(G, "empty") && x.isHostMethod(H, "select") && P && F))
            return b.fail("No means of selecting a Range or TextRange was found"),
            !1;
        R.removeAllRanges = function() {
            try {
                if (this.docSelection.empty(),
                "None" != this.docSelection.type) {
                    var a;
                    if (this.anchorNode)
                        a = w.getDocument(this.anchorNode);
                    else if (this.docSelection.type == C) {
                        var b = this.docSelection.createRange();
                        b.length && (a = w.getDocument(b.item(0)).body.createTextRange())
                    }
                    if (a) {
                        var c = a.body.createTextRange();
                        c.select(),
                        this.docSelection.empty()
                    }
                }
            } catch (d) {}
            g(this)
        }
        ,
        R.addRange = function(a) {
            this.docSelection.type == C ? n(this, a) : (z.rangeToTextRange(a).select(),
            this._ranges[0] = a,
            this.rangeCount = 1,
            this.isCollapsed = this._ranges[0].collapsed,
            e(this, a, !1))
        }
        ,
        R.setRanges = function(a) {
            this.removeAllRanges();
            var b = a.length;
            b > 1 ? p(this, a) : b && this.addRange(a[0])
        }
    }
    R.getRangeAt = function(a) {
        if (0 > a || a >= this.rangeCount)
            throw new A("INDEX_SIZE_ERR");
        return this._ranges[a]
    }
    ;
    var T;
    if (F)
        T = function(b) {
            var c;
            a.isSelectionValid(b.win) ? c = b.docSelection.createRange() : (c = w.getBody(b.win.document).createTextRange(),
            c.collapse(!0)),
            b.docSelection.type == C ? m(b) : k(c) ? l(b, c) : g(b)
        }
        ;
    else if (x.isHostMethod(G, "getRangeAt") && "number" == typeof G.rangeCount)
        T = function(b) {
            if (P && E && b.docSelection.type == C)
                m(b);
            else if (b._ranges.length = b.rangeCount = b.nativeSelection.rangeCount,
            b.rangeCount) {
                for (var c = 0, d = b.rangeCount; d > c; ++c)
                    b._ranges[c] = new a.WrappedRange(b.nativeSelection.getRangeAt(c));
                e(b, b._ranges[b.rangeCount - 1], V(b.nativeSelection)),
                b.isCollapsed = t(b)
            } else
                g(b)
        }
        ;
    else {
        if (!J || typeof G.isCollapsed != u || typeof H.collapsed != u || !a.features.implementsDomRange)
            return b.fail("No means of obtaining a Range or TextRange from the user's selection was found"),
            !1;
        T = function(a) {
            var b, c = a.nativeSelection;
            c.anchorNode ? (b = Q(c, 0),
            a._ranges = [b],
            a.rangeCount = 1,
            f(a),
            a.isCollapsed = t(a)) : g(a)
        }
    }
    R.refresh = function(a) {
        var b = a ? this._ranges.slice(0) : null;
        if (T(this),
        a) {
            var c = b.length;
            if (c != this._ranges.length)
                return !1;
            for (; c--; )
                if (!y.rangesEqual(b[c], this._ranges[c]))
                    return !1;
            return !0
        }
    }
    ;
    var U = function(a, b) {
        var c = a.getAllRanges()
          , d = !1;
        a.removeAllRanges();
        for (var e = 0, f = c.length; f > e; ++e)
            d || b !== c[e] ? a.addRange(c[e]) : d = !0;
        a.rangeCount || g(a)
    };
    R.removeRange = P ? function(a) {
        if (this.docSelection.type == C) {
            for (var b, c = this.docSelection.createRange(), d = j(a), e = w.getDocument(c.item(0)), f = w.getBody(e).createControlRange(), g = !1, h = 0, i = c.length; i > h; ++h)
                b = c.item(h),
                b !== d || g ? f.add(c.item(h)) : g = !0;
            f.select(),
            m(this)
        } else
            U(this, a)
    }
    : function(a) {
        U(this, a)
    }
    ;
    var V;
    !F && J && a.features.implementsDomRange ? (V = function(a) {
        var b = !1;
        return a.anchorNode && (b = 1 == w.comparePoints(a.anchorNode, a.anchorOffset, a.focusNode, a.focusOffset)),
        b
    }
    ,
    R.isBackwards = function() {
        return V(this)
    }
    ) : V = R.isBackwards = function() {
        return !1
    }
    ,
    R.toString = function() {
        for (var a = [], b = 0, c = this.rangeCount; c > b; ++b)
            a[b] = "" + this._ranges[b];
        return a.join("")
    }
    ,
    R.collapse = function(b, c) {
        q(this, b);
        var d = a.createRange(w.getDocument(b));
        d.collapseToPoint(b, c),
        this.removeAllRanges(),
        this.addRange(d),
        this.isCollapsed = !0
    }
    ,
    R.collapseToStart = function() {
        if (!this.rangeCount)
            throw new A("INVALID_STATE_ERR");
        var a = this._ranges[0];
        this.collapse(a.startContainer, a.startOffset)
    }
    ,
    R.collapseToEnd = function() {
        if (!this.rangeCount)
            throw new A("INVALID_STATE_ERR");
        var a = this._ranges[this.rangeCount - 1];
        this.collapse(a.endContainer, a.endOffset)
    }
    ,
    R.selectAllChildren = function(b) {
        q(this, b);
        var c = a.createRange(w.getDocument(b));
        c.selectNodeContents(b),
        this.removeAllRanges(),
        this.addRange(c)
    }
    ,
    R.deleteFromDocument = function() {
        if (P && E && this.docSelection.type == C) {
            for (var a, b = this.docSelection.createRange(); b.length; )
                a = b.item(0),
                b.remove(a),
                a.parentNode.removeChild(a);
            this.refresh()
        } else if (this.rangeCount) {
            var c = this.getAllRanges();
            this.removeAllRanges();
            for (var d = 0, e = c.length; e > d; ++d)
                c[d].deleteContents();
            this.addRange(c[e - 1])
        }
    }
    ,
    R.getAllRanges = function() {
        return this._ranges.slice(0)
    }
    ,
    R.setSingleRange = function(a) {
        this.setRanges([a])
    }
    ,
    R.containsNode = function(a, b) {
        for (var c = 0, d = this._ranges.length; d > c; ++c)
            if (this._ranges[c].containsNode(a, b))
                return !0;
        return !1
    }
    ,
    R.toHtml = function() {
        var a = "";
        if (this.rangeCount) {
            for (var b = y.getRangeDocument(this._ranges[0]).createElement("div"), c = 0, d = this._ranges.length; d > c; ++c)
                b.appendChild(this._ranges[c].cloneContents());
            a = b.innerHTML
        }
        return a
    }
    ,
    R.getName = function() {
        return "WrappedSelection"
    }
    ,
    R.inspect = function() {
        return r(this)
    }
    ,
    R.detach = function() {
        this.win[v] = null,
        this.win = this.anchorNode = this.focusNode = null
    }
    ,
    o.inspect = r,
    a.Selection = o,
    a.selectionPrototype = R,
    a.addCreateMissingNativeApiListener(function(b) {
        "undefined" == typeof b.getSelection && (b.getSelection = function() {
            return a.getSelection(this)
        }
        ),
        b = null
    })
}),
"function" != typeof String.prototype.trim && (String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, "")
}
),
"indexOf"in Array.prototype || (Array.prototype.indexOf = function(a, b) {
    void 0 === b && (b = 0),
    0 > b && (b += this.length),
    0 > b && (b = 0);
    for (var c = this.length; c > b; b++)
        if (b in this && this[b] === a)
            return b;
    return -1
}
),
"lastIndexOf"in Array.prototype || (Array.prototype.lastIndexOf = function(a, b) {
    for (void 0 === b && (b = this.length - 1),
    0 > b && (b += this.length),
    b > this.length - 1 && (b = this.length - 1),
    b++; b-- > 0; )
        if (b in this && this[b] === a)
            return b;
    return -1
}
),
"map"in Array.prototype || (Array.prototype.map = function(a, b) {
    for (var c = new Array(this.length), d = 0, e = this.length; e > d; d++)
        d in this && (c[d] = a.call(b, this[d], d, this));
    return c
}
),
"filter"in Array.prototype || (Array.prototype.filter = function(a, b) {
    for (var c, d = [], e = 0, f = this.length; f > e; e++)
        e in this && a.call(b, c = this[e], e, this) && d.push(c);
    return d
}
),
function() {
    var a, b, c = this;
    a = {
        changeIdAttribute: "data-cid",
        userIdAttribute: "data-userid",
        userNameAttribute: "data-username",
        timeAttribute: "data-time",
        attrValuePrefix: "",
        blockEl: "p",
        blockEls: ["p", "ol", "ul", "li", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote"],
        stylePrefix: "cts",
        currentUser: {
            id: null,
            name: null
        },
        changeTypes: {
            insertType: {
                tag: "span",
                alias: "ins",
                action: "Inserted"
            },
            deleteType: {
                tag: "span",
                alias: "del",
                action: "Deleted"
            }
        },
        handleEvents: !1,
        contentEditable: !0,
        isTracking: !0,
        noTrack: ".ice-no-track",
        avoid: ".ice-avoid",
        mergeBlocks: !0
    },
    b = function(b) {
        if (this._changes = {},
        b || (b = {}),
        !b.element)
            throw Error("`options.element` must be defined for ice construction.");
        ice.dom.extend(!0, this, a, b),
        this.pluginsManager = new ice.IcePluginManager(this),
        b.plugins && this.pluginsManager.usePlugins("ice-init", b.plugins)
    }
    ,
    b.prototype = {
        _userStyles: {},
        _styles: {},
        _uniqueStyleIndex: 0,
        _browserType: null,
        _batchChangeid: null,
        _uniqueIDIndex: 1,
        _delBookmark: "tempdel",
        isPlaceHoldingDeletes: !1,
        startTracking: function() {
            if (this.element.setAttribute("contentEditable", this.contentEditable),
            this.handleEvents) {
                var a = this;
                ice.dom.bind(a.element, "keyup.ice keydown.ice keypress.ice mousedown.ice mouseup.ice", function(b) {
                    return a.handleEvent(b)
                })
            }
            return this.initializeEnvironment(),
            this.initializeEditor(),
            this.findTrackTags(),
            this.initializeRange(),
            this.pluginsManager.fireEnabled(this.element),
            this
        },
        stopTracking: function() {
            if (this.element.setAttribute("contentEditable", !this.contentEditable),
            this.handleEvents) {
                var a = this;
                ice.dom.unbind(a.element, "keyup.ice keydown.ice keypress.ice mousedown.ice mouseup.ice")
            }
            return this.pluginsManager.fireDisabled(this.element),
            this
        },
        initializeEnvironment: function() {
            this.env || (this.env = {}),
            this.env.element = this.element,
            this.env.document = this.element.ownerDocument,
            this.env.window = this.env.document.defaultView || this.env.document.parentWindow || window,
            this.env.frame = this.env.window.frameElement,
            this.env.selection = this.selection = new ice.Selection(this.env),
            this.env.document.createElement(this.changeTypes.insertType.tag),
            this.env.document.createElement(this.changeTypes.deleteType.tag)
        },
        initializeRange: function() {
            var a = this.selection.createRange();
            a.setStart(ice.dom.find(this.element, this.blockEls.join(", "))[0], 0),
            a.collapse(!0),
            this.selection.addRange(a),
            this.env.frame ? this.env.frame.contentWindow.focus() : this.element.focus()
        },
        initializeEditor: function() {
            var a = this.env.document.createElement("div");
            this.element.childNodes.length ? (a.innerHTML = this.element.innerHTML,
            ice.dom.removeWhitespace(a),
            "" === a.innerHTML && a.appendChild(ice.dom.create("<" + this.blockEl + " ><br/></" + this.blockEl + ">"))) : a.appendChild(ice.dom.create("<" + this.blockEl + " ><br/></" + this.blockEl + ">")),
            this.element.innerHTML != a.innerHTML && (this.element.innerHTML = a.innerHTML)
        },
        findTrackTags: function() {
            var a = this
              , b = [];
            for (var c in this.changeTypes)
                b.push(this._getIceNodeClass(c));
            ice.dom.each(ice.dom.find(this.element, "." + b.join(", .")), function(c, d) {
                for (var e = 0, f = "", g = d.className.split(" "), c = 0; c < g.length; c++) {
                    var h = new RegExp(a.stylePrefix + "-(\\d+)").exec(g[c]);
                    h && (e = h[1]);
                    var i = new RegExp("(" + b.join("|") + ")").exec(g[c]);
                    i && (f = a._getChangeTypeFromAlias(i[1]))
                }
                var j = ice.dom.attr(d, a.userIdAttribute);
                a.setUserStyle(j, Number(e));
                var k = ice.dom.attr(d, a.changeIdAttribute);
                a._changes[k] = {
                    type: f,
                    userid: j,
                    username: ice.dom.attr(d, a.userNameAttribute),
                    time: ice.dom.attr(d, a.timeAttribute)
                }
            })
        },
        enableChangeTracking: function() {
            this.isTracking = !0,
            this.pluginsManager.fireEnabled(this.element)
        },
        disableChangeTracking: function() {
            this.isTracking = !1,
            this.pluginsManager.fireDisabled(this.element)
        },
        setCurrentUser: function(a) {
            this.currentUser = a
        },
        handleEvent: function(a) {
            if (this.isTracking)
                if ("mouseup" == a.type) {
                    var b = this;
                    setTimeout(function() {
                        b.mouseUp(a)
                    }, 200)
                } else {
                    if ("mousedown" == a.type)
                        return this.mouseDown(a);
                    if ("keypress" == a.type) {
                        var c = this.keyPress(a);
                        return c || a.preventDefault(),
                        c
                    }
                    if ("keydown" == a.type) {
                        var c = this.keyDown(a);
                        return c || a.preventDefault(),
                        c
                    }
                    "keyup" == a.type && this.pluginsManager.fireCaretUpdated()
                }
        },
        visible: function(a) {
            a.nodeType === ice.dom.TEXT_NODE && (a = a.parentNode);
            var b = a.getBoundingClientRect();
            return b.top > 0 && b.left > 0
        },
        createIceNode: function(a, b) {
            var c = this.env.document.createElement(this.changeTypes[a].tag);
            return ice.dom.addClass(c, this._getIceNodeClass(a)),
            c.appendChild(b ? b : this.env.document.createTextNode("")),
            this.addChange(this.changeTypes[a].alias, [c]),
            this.pluginsManager.fireNodeCreated(c, {
                action: this.changeTypes[a].action
            }),
            c
        },
        insert: function(a, b) {
            var c = !a;
            if (a || (a = "\ufeff"),
            b ? this.selection.addRange(b) : b = this.getCurrentRange(),
            "string" == typeof a && (a = document.createTextNode(a)),
            !b.collapsed && (this.deleteContents(),
            b = this.getCurrentRange(),
            b.startContainer === b.endContainer && this.element === b.startContainer)) {
                ice.dom.empty(this.element);
                var d = b.getLastSelectableChild(this.element);
                b.setStartAfter(d),
                b.collapse(!0)
            }
            this._moveRangeToValidTrackingPos(b);
            var e = this.startBatchChange();
            return this._insertNode(a, b, c),
            this.pluginsManager.fireNodeInserted(a, b),
            this.endBatchChange(e),
            c
        },
        placeholdDeletes: function() {
            var a = this;
            this.isPlaceholdingDeletes && this.revertDeletePlaceholders(),
            this.isPlaceholdingDeletes = !0,
            this._deletes = [];
            var b = "." + this._getIceNodeClass("deleteType");
            return ice.dom.each(ice.dom.find(this.element, b), function(b, c) {
                a._deletes.push(ice.dom.cloneNode(c)),
                ice.dom.replaceWith(c, "<" + a._delBookmark + ' data-allocation="' + (a._deletes.length - 1) + '"/>')
            }),
            !0
        },
        revertDeletePlaceholders: function() {
            var a = this;
            return this.isPlaceholdingDeletes ? (ice.dom.each(this._deletes, function(b, c) {
                ice.dom.find(a.element, a._delBookmark + "[data-allocation=" + b + "]").replaceWith(c)
            }),
            this.isPlaceholdingDeletes = !1,
            !0) : !1
        },
        deleteContents: function(a, b) {
            var c = !0
              , d = ice.dom.browser();
            b ? this.selection.addRange(b) : b = this.getCurrentRange();
            var e = this.startBatchChange(this.changeTypes.deleteType.alias);
            if (b.collapsed === !1)
                this._currentUserIceNode(b.startContainer.parentNode) ? this._deleteSelection(b) : (this._deleteSelection(b),
                "mozilla" === d.type ? (b.startContainer.parentNode.previousSibling ? (b.setEnd(b.startContainer.parentNode.previousSibling, 0),
                b.moveEnd(ice.dom.CHARACTER_UNIT, ice.dom.getNodeCharacterLength(b.endContainer))) : b.setEndAfter(b.startContainer.parentNode),
                b.collapse(!1)) : this.visible(b.endContainer) || (b.setEnd(b.endContainer, b.endOffset - 1),
                b.collapse(!1)));
            else if (a)
                if ("mozilla" === d.type)
                    c = this._deleteRight(b),
                    this.visible(b.endContainer) || (b.endContainer.parentNode.nextSibling ? b.setEndBefore(b.endContainer.parentNode.nextSibling) : b.setEndAfter(b.endContainer),
                    b.collapse(!1));
                else {
                    if (b.endOffset === ice.dom.getNodeCharacterLength(b.endContainer)) {
                        var f = b.startContainer.nextSibling;
                        if (ice.dom.is(f, "." + this._getIceNodeClass("deleteType")))
                            for (; f; ) {
                                if (!ice.dom.is(f, "." + this._getIceNodeClass("deleteType"))) {
                                    b.setStart(f, 0),
                                    b.collapse(!0);
                                    break
                                }
                                f = f.nextSibling
                            }
                    }
                    c = this._deleteRight(b),
                    this.visible(b.endContainer) || ice.dom.is(b.endContainer.parentNode, "." + this._getIceNodeClass("insertType") + ", ." + this._getIceNodeClass("deleteType")) && (b.setStartAfter(b.endContainer.parentNode),
                    b.collapse(!0))
                }
            else if ("mozilla" === d.type)
                c = this._deleteLeft(b),
                this.visible(b.startContainer) || (b.startContainer.parentNode.previousSibling ? b.setEnd(b.startContainer.parentNode.previousSibling, 0) : b.setEnd(b.startContainer.parentNode, 0),
                b.moveEnd(ice.dom.CHARACTER_UNIT, ice.dom.getNodeCharacterLength(b.endContainer)),
                b.collapse(!1));
            else {
                if (!this.visible(b.startContainer) && b.endOffset === ice.dom.getNodeCharacterLength(b.endContainer)) {
                    var g = b.startContainer.previousSibling;
                    if (ice.dom.is(g, "." + this._getIceNodeClass("deleteType")))
                        for (; g; ) {
                            if (!ice.dom.is(g, "." + this._getIceNodeClass("deleteType"))) {
                                b.setEndBefore(g.nextSibling, 0),
                                b.collapse(!1);
                                break
                            }
                            g = g.prevSibling
                        }
                }
                c = this._deleteLeft(b)
            }
            return this.selection.addRange(b),
            this.endBatchChange(e),
            c
        },
        getChanges: function() {
            return this._changes
        },
        getChangeUserids: function() {
            var a = []
              , b = Object.keys(this._changes);
            for (var c in b)
                a.push(this._changes[b[c]].userid);
            return a.sort().filter(function(a, b, c) {
                return b == c.indexOf(a) ? 1 : 0
            })
        },
        getElementContent: function() {
            return this.element.innerHTML
        },
        getCleanContent: function(a, b, c) {
            var d = ""
              , e = this;
            ice.dom.each(this.changeTypes, function(a, b) {
                "deleteType" != a && (b > 0 && (d += ","),
                d += "." + e._getIceNodeClass(a))
            }),
            a = a ? "string" == typeof a ? ice.dom.create("<div>" + a + "</div>") : ice.dom.cloneNode(a, !1)[0] : ice.dom.cloneNode(this.element, !1)[0],
            a = c ? c.call(this, a) : a;
            var f = ice.dom.find(a, d);
            ice.dom.each(f, function() {
                ice.dom.replaceWith(this, ice.dom.contents(this))
            });
            var g = ice.dom.find(a, "." + this._getIceNodeClass("deleteType"));
            return ice.dom.remove(g),
            a = b ? b.call(this, a) : a,
            a.innerHTML
        },
        acceptAll: function() {
            this.element.innerHTML = this.getCleanContent()
        },
        rejectAll: function() {
            var a = "." + this._getIceNodeClass("insertType")
              , b = "." + this._getIceNodeClass("deleteType");
            ice.dom.remove(ice.dom.find(this.element, a)),
            ice.dom.each(ice.dom.find(this.element, b), function(a, b) {
                ice.dom.replaceWith(b, ice.dom.contents(b))
            })
        },
        acceptChange: function(a) {
            this.acceptRejectChange(a, !0)
        },
        rejectChange: function(a) {
            this.acceptRejectChange(a, !1)
        },
        acceptRejectChange: function(a, b) {
            var c, d, e, f, g, h, i, j = ice.dom;
            if (!a) {
                var k = this.getCurrentRange();
                if (!k.collapsed)
                    return;
                a = k.startContainer
            }
            c = f = "." + this._getIceNodeClass("deleteType"),
            d = g = "." + this._getIceNodeClass("insertType"),
            e = c + "," + d,
            h = j.getNode(a, e),
            i = j.find(this.element, "[" + this.changeIdAttribute + "=" + j.attr(h, this.changeIdAttribute) + "]"),
            b || (f = d,
            g = c),
            ice.dom.is(h, g) ? j.each(i, function(a, b) {
                j.replaceWith(b, ice.dom.contents(b))
            }) : j.is(h, f) && j.remove(i)
        },
        isInsideChange: function(a) {
            var b = "." + this._getIceNodeClass("insertType") + ", ." + this._getIceNodeClass("deleteType");
            if (!a) {
                if (range = this.getCurrentRange(),
                !range.collapsed)
                    return !1;
                a = range.startContainer
            }
            return !!ice.dom.getNode(a, b)
        },
        addChangeType: function(a, b, c, d) {
            var e = {
                tag: b,
                alias: c
            };
            d && (e.action = d),
            this.changeTypes[a] = e
        },
        getIceNode: function(a, b) {
            var c = "." + this._getIceNodeClass(b);
            return ice.dom.getNode(a, c)
        },
        _moveRangeToValidTrackingPos: function(a) {
            for (var b = !1, c = this._getVoidElement(a.endContainer); c; ) {
                try {
                    a.moveEnd(ice.dom.CHARACTER_UNIT, 1),
                    a.moveEnd(ice.dom.CHARACTER_UNIT, -1)
                } catch (d) {
                    b = !0
                }
                if (b || ice.dom.onBlockBoundary(a.endContainer, a.startContainer, this.blockEls)) {
                    a.setStartAfter(c),
                    a.collapse(!0);
                    break
                }
                c = this._getVoidElement(a.endContainer),
                c ? (a.setEnd(a.endContainer, 0),
                a.moveEnd(ice.dom.CHARACTER_UNIT, ice.dom.getNodeCharacterLength(a.endContainer)),
                a.collapse()) : (a.setStart(a.endContainer, 0),
                a.collapse(!0))
            }
        },
        _getNoTrackElement: function(a) {
            var b = this._getNoTrackSelector()
              , c = ice.dom.is(a, b) ? a : ice.dom.parents(a, b)[0] || null;
            return c
        },
        _getNoTrackSelector: function() {
            return this.noTrack
        },
        _getVoidElement: function(a) {
            var b = this._getVoidElSelector();
            return ice.dom.is(a, b) ? a : ice.dom.parents(a, b)[0] || null
        },
        _getVoidElSelector: function() {
            return "." + this._getIceNodeClass("deleteType") + "," + this.avoid
        },
        _currentUserIceNode: function(a) {
            return ice.dom.attr(a, this.userIdAttribute) == this.currentUser.id
        },
        _getChangeTypeFromAlias: function(a) {
            var b, c = null;
            for (b in this.changeTypes)
                this.changeTypes.hasOwnProperty(b) && this.changeTypes[b].alias == a && (c = b);
            return c
        },
        _getIceNodeClass: function(a) {
            return this.attrValuePrefix + this.changeTypes[a].alias
        },
        getUserStyle: function(a) {
            var b = null;
            return b = this._userStyles[a] ? this._userStyles[a] : this.setUserStyle(a, this.getNewStyleId())
        },
        setUserStyle: function(a, b) {
            var c = this.stylePrefix + "-" + b;
            return this._styles[b] || (this._styles[b] = !0),
            this._userStyles[a] = c
        },
        getNewStyleId: function() {
            var a = ++this._uniqueStyleIndex;
            return this._styles[a] ? this.getNewStyleId() : (this._styles[a] = !0,
            a)
        },
        addChange: function(a, b) {
            var c = this._batchChangeid || this.getNewChangeId();
            this._changes[c] || (this._changes[c] = {
                type: this._getChangeTypeFromAlias(a),
                time: (new Date).getTime(),
                userid: this.currentUser.id,
                username: this.currentUser.name
            });
            var d = this;
            return ice.dom.foreach(b, function(a) {
                d.addNodeToChange(c, b[a])
            }),
            c
        },
        addNodeToChange: function(a, b) {
            null !== this._batchChangeid && (a = this._batchChangeid);
            var c = this.getChange(a);
            b.getAttribute(this.changeIdAttribute) || b.setAttribute(this.changeIdAttribute, a),
            b.getAttribute(this.userIdAttribute) || b.setAttribute(this.userIdAttribute, c.userid),
            b.getAttribute(this.userNameAttribute) || b.setAttribute(this.userNameAttribute, c.username),
            b.getAttribute(this.timeAttribute) || b.setAttribute(this.timeAttribute, c.time),
            ice.dom.hasClass(b, this._getIceNodeClass(c.type)) || ice.dom.addClass(b, this._getIceNodeClass(c.type));
            var d = this.getUserStyle(c.userid);
            ice.dom.hasClass(b, d) || ice.dom.addClass(b, d)
        },
        getChange: function(a) {
            var b = null;
            return this._changes[a] && (b = this._changes[a]),
            b
        },
        getNewChangeId: function() {
            var a = ++this._uniqueIDIndex;
            return this._changes[a] && (a = this.getNewChangeId()),
            a
        },
        startBatchChange: function() {
            return this._batchChangeid = this.getNewChangeId(),
            this._batchChangeid
        },
        endBatchChange: function(a) {
            a === this._batchChangeid && (this._batchChangeid = null)
        },
        getCurrentRange: function() {
            return this.selection.getRangeAt(0)
        },
        _insertNode: function(a, b, c) {
            ice.dom.isBlockElement(b.startContainer) || ice.dom.canContainTextElement(ice.dom.getBlockParent(b.startContainer, this.element)) || !b.startContainer.previousSibling || b.setStart(b.startContainer.previousSibling, 0);
            var d = (b.startContainer,
            ice.dom.isBlockElement(b.startContainer) && b.startContainer || ice.dom.getBlockParent(b.startContainer, this.element) || null);
            if (d === this.element) {
                var e = document.createElement(this.blockEl);
                return d.appendChild(e),
                b.setStart(e, 0),
                b.collapse(),
                this._insertNode(a, b, c)
            }
            ice.dom.hasNoTextOrStubContent(d) && (ice.dom.empty(d),
            ice.dom.append(d, "<br>"),
            b.setStart(d, 0));
            var f = this.getIceNode(b.startContainer, "insertType")
              , g = this._currentUserIceNode(f);
            c && g || (g || (a = this.createIceNode("insertType", a)),
            b.insertNode(a),
            b.setEnd(a, 1),
            c ? b.setStart(a, 0) : b.collapse(),
            this.selection.addRange(b))
        },
        _handleVoidEl: function(a, b) {
            var c = this._getVoidElement(a);
            return c && !this.getIceNode(c, "deleteType") ? (b.collapse(!0),
            !0) : !1
        },
        _deleteSelection: function(a) {
            for (var b = new ice.Bookmark(this.env,a), c = ice.dom.getElementsBetween(b.start, b.end), d = ice.dom.parents(a.startContainer, this.blockEls.join(", "))[0], e = ice.dom.parents(a.endContainer, this.blockEls.join(", "))[0], f = new Array, g = 0; g < c.length; g++) {
                var h = c[g];
                if (!ice.dom.isBlockElement(h) || (f.push(h),
                ice.dom.canContainTextElement(h))) {
                    if ((h.nodeType !== ice.dom.TEXT_NODE || 0 !== ice.dom.getNodeTextContent(h).length) && !this._getVoidElement(h)) {
                        if (h.nodeType !== ice.dom.TEXT_NODE) {
                            if (ice.dom.BREAK_ELEMENT == ice.dom.getTagName(h))
                                continue;
                            if (ice.dom.isStubElement(h)) {
                                this._addNodeTracking(h, !1, !0);
                                continue
                            }
                            for (ice.dom.hasNoTextOrStubContent(h) && ice.dom.remove(h),
                            j = 0; j < h.childNodes.length; j++) {
                                var i = h.childNodes[j];
                                c.push(i)
                            }
                            continue
                        }
                        var k = ice.dom.getBlockParent(h);
                        this._addNodeTracking(h, !1, !0, !0),
                        ice.dom.hasNoTextOrStubContent(k) && ice.dom.remove(k)
                    }
                } else
                    for (var l = 0; l < h.childNodes.length; l++)
                        c.push(h.childNodes[l])
            }
            if (this.mergeBlocks && d !== e) {
                for (; f.length; )
                    ice.dom.mergeContainers(f.shift(), d);
                ice.dom.removeBRFromChild(e),
                ice.dom.removeBRFromChild(d),
                ice.dom.mergeContainers(e, d)
            }
            b.selectBookmark(),
            a.collapse(!0)
        },
        _deleteRight: function(a) {
            var b, c, d = ice.dom.isBlockElement(a.startContainer) && a.startContainer || ice.dom.getBlockParent(a.startContainer, this.element) || null, e = d ? ice.dom.hasNoTextOrStubContent(d) : !1, f = d && ice.dom.getNextContentNode(d, this.element), g = f ? ice.dom.hasNoTextOrStubContent(f) : !1, h = a.endContainer, i = a.endOffset, j = a.commonAncestorContainer;
            if (e)
                return !1;
            if (j.nodeType !== ice.dom.TEXT_NODE) {
                if (0 === i && ice.dom.isBlockElement(j) && !ice.dom.canContainTextElement(j)) {
                    var k = j.firstElementChild;
                    if (k)
                        return a.setStart(k, 0),
                        a.collapse(),
                        this._deleteRight(a)
                }
                if (j.childNodes.length > i) {
                    var l = document.createTextNode(" ");
                    return j.insertBefore(l, j.childNodes[i]),
                    a.setStart(l, 1),
                    a.collapse(!0),
                    c = this._deleteRight(a),
                    ice.dom.remove(l),
                    c
                }
                return b = ice.dom.getNextContentNode(j, this.element),
                a.setEnd(b, 0),
                a.collapse(),
                this._deleteRight(a)
            }
            if (a.moveEnd(ice.dom.CHARACTER_UNIT, 1),
            a.moveEnd(ice.dom.CHARACTER_UNIT, -1),
            i === h.data.length && !ice.dom.hasNoTextOrStubContent(h)) {
                if (b = ice.dom.getNextNode(h, this.element),
                !b)
                    return a.selectNodeContents(h),
                    a.collapse(),
                    !1;
                if (ice.dom.BREAK_ELEMENT == ice.dom.getTagName(b) && (b = ice.dom.getNextNode(b, this.element)),
                b.nodeType === ice.dom.TEXT_NODE && (b = b.parentNode),
                !b.isContentEditable) {
                    c = this._addNodeTracking(b, !1, !1);
                    var m = document.createTextNode("");
                    return b.parentNode.insertBefore(m, b.nextSibling),
                    a.selectNode(m),
                    a.collapse(!0),
                    c
                }
                if (this._handleVoidEl(b, a))
                    return !0;
                if (ice.dom.isChildOf(b, d) && ice.dom.isStubElement(b))
                    return this._addNodeTracking(b, a, !1)
            }
            if (this._handleVoidEl(b, a))
                return !0;
            if (this._getNoTrackElement(a.endContainer.parentElement))
                return a.deleteContents(),
                !1;
            if (ice.dom.isOnBlockBoundary(a.startContainer, a.endContainer, this.element)) {
                if (this.mergeBlocks && ice.dom.is(ice.dom.getBlockParent(b, this.element), this.blockEl)) {
                    f !== ice.dom.getBlockParent(a.endContainer, this.element) && a.setEnd(f, 0);
                    for (var n = ice.dom.getElementsBetween(a.startContainer, a.endContainer), o = 0; o < n.length; o++)
                        ice.dom.remove(n[o]);
                    var p = a.startContainer
                      , q = a.endContainer;
                    return ice.dom.remove(ice.dom.find(p, "br")),
                    ice.dom.remove(ice.dom.find(q, "br")),
                    ice.dom.mergeBlockWithSibling(a, ice.dom.getBlockParent(a.endContainer, this.element) || d)
                }
                return g ? (ice.dom.remove(f),
                a.collapse(!0),
                !0) : (a.setStart(f, 0),
                a.collapse(!0),
                !0)
            }
            {
                var r = a.endContainer
                  , s = r.splitText(a.endOffset);
                s.splitText(1)
            }
            return this._addNodeTracking(s, a, !1)
        },
        _deleteLeft: function(a) {
            var b, c, d = ice.dom.isBlockElement(a.startContainer) && a.startContainer || ice.dom.getBlockParent(a.startContainer, this.element) || null, e = d ? ice.dom.hasNoTextOrStubContent(d) : !1, f = d && ice.dom.getPrevContentNode(d, this.element), g = f ? ice.dom.hasNoTextOrStubContent(f) : !1, h = a.startContainer, i = a.startOffset, j = a.commonAncestorContainer;
            if (e)
                return !1;
            if (0 === i || j.nodeType !== ice.dom.TEXT_NODE) {
                if (ice.dom.isBlockElement(j) && !ice.dom.canContainTextElement(j))
                    if (0 === i) {
                        var k = j.firstElementChild;
                        if (k)
                            return a.setStart(k, 0),
                            a.collapse(),
                            this._deleteLeft(a)
                    } else {
                        var l = j.lastElementChild;
                        if (l && (b = a.getLastSelectableChild(l)))
                            return a.setStart(b, b.data.length),
                            a.collapse(),
                            this._deleteLeft(a)
                    }
                if (0 === i)
                    c = ice.dom.getPrevContentNode(h, this.element);
                else {
                    c = j.childNodes[i - 1]
                }
                if (!c)
                    return !1;
                if (ice.dom.is(c, "." + this._getIceNodeClass("insertType") + ", ." + this._getIceNodeClass("deleteType")) && c.childNodes.length > 0 && c.lastChild && (c = c.lastChild),
                c.nodeType === ice.dom.TEXT_NODE && (c = c.parentNode),
                !c.isContentEditable) {
                    var m = this._addNodeTracking(c, !1, !0)
                      , n = document.createTextNode("");
                    return c.parentNode.insertBefore(n, c),
                    a.selectNode(n),
                    a.collapse(!0),
                    m
                }
                if (this._handleVoidEl(c, a))
                    return !0;
                if (ice.dom.isStubElement(c) && ice.dom.isChildOf(c, d) || !c.isContentEditable)
                    return this._addNodeTracking(c, a, !0);
                if (ice.dom.isStubElement(c))
                    return ice.dom.remove(c),
                    a.collapse(!0),
                    !1;
                if (c !== d && !ice.dom.isChildOf(c, d)) {
                    if (ice.dom.canContainTextElement(c) || (c = c.lastElementChild),
                    c.lastChild && c.lastChild.nodeType !== ice.dom.TEXT_NODE && ice.dom.isStubElement(c.lastChild) && "BR" !== c.lastChild.tagName)
                        return a.setStartAfter(c.lastChild),
                        a.collapse(!0),
                        !0;
                    if (b = a.getLastSelectableChild(c),
                    b && !ice.dom.isOnBlockBoundary(a.startContainer, b, this.element))
                        return a.selectNodeContents(b),
                        a.collapse(),
                        !0
                }
            }
            if (1 === i && !ice.dom.isBlockElement(j) && a.startContainer.childNodes.length > 1 && a.startContainer.childNodes[0].nodeType === ice.dom.TEXT_NODE && 0 === a.startContainer.childNodes[0].data.length)
                return a.setStart(a.startContainer, 0),
                this._deleteLeft(a);
            if (a.moveStart(ice.dom.CHARACTER_UNIT, -1),
            a.moveStart(ice.dom.CHARACTER_UNIT, 1),
            this._getNoTrackElement(a.startContainer.parentElement))
                return a.deleteContents(),
                !1;
            if (ice.dom.isOnBlockBoundary(a.startContainer, a.endContainer, this.element)) {
                if (g)
                    return ice.dom.remove(f),
                    a.collapse(),
                    !0;
                if (this.mergeBlocks && ice.dom.is(ice.dom.getBlockParent(c, this.element), this.blockEl)) {
                    f !== ice.dom.getBlockParent(a.startContainer, this.element) && a.setStart(f, f.childNodes.length);
                    for (var o = ice.dom.getElementsBetween(a.startContainer, a.endContainer), p = 0; p < o.length; p++)
                        ice.dom.remove(o[p]);
                    var q = a.startContainer
                      , r = a.endContainer;
                    return ice.dom.remove(ice.dom.find(q, "br")),
                    ice.dom.remove(ice.dom.find(r, "br")),
                    ice.dom.mergeBlockWithSibling(a, ice.dom.getBlockParent(a.endContainer, this.element) || d)
                }
                return f && f.lastChild && ice.dom.isStubElement(f.lastChild) ? (a.setStartAfter(f.lastChild),
                a.collapse(!0),
                !0) : (b = a.getLastSelectableChild(f),
                b ? (a.setStart(b, b.data.length),
                a.collapse(!0)) : f && (a.setStart(f, f.childNodes.length),
                a.collapse(!0)),
                !0)
            }
            {
                var s = a.startContainer
                  , t = s.splitText(a.startOffset - 1);
                t.splitText(1)
            }
            return this._addNodeTracking(t, a, !0)
        },
        _addNodeTracking: function(a, b, c) {
            var d = this.getIceNode(a, "insertType");
            if (d && this._currentUserIceNode(d)) {
                b && c && b.selectNode(a),
                a.parentNode.removeChild(a);
                var e = ice.dom.cloneNode(d);
                if (ice.dom.remove(ice.dom.find(e, ".iceBookmark")),
                null !== d && ice.dom.hasNoTextOrStubContent(e[0])) {
                    var f = this.env.document.createTextNode("");
                    ice.dom.insertBefore(d, f),
                    b && (b.setStart(f, 0),
                    b.collapse(!0)),
                    ice.dom.replaceWith(d, ice.dom.contents(d))
                }
                return !0
            }
            if (b && this.getIceNode(a, "deleteType")) {
                a.normalize();
                var g = !1;
                if (c) {
                    for (var h = ice.dom.getPrevContentNode(a, this.element); !g; )
                        k = this.getIceNode(h, "deleteType"),
                        k ? h = ice.dom.getPrevContentNode(h, this.element) : g = !0;
                    if (h) {
                        var i = b.getLastSelectableChild(h);
                        i && (h = i),
                        b.setStart(h, ice.dom.getNodeCharacterLength(h)),
                        b.collapse(!0)
                    }
                    return !0
                }
                for (var j = ice.dom.getNextContentNode(a, this.element); !g; )
                    k = this.getIceNode(j, "deleteType"),
                    k ? j = ice.dom.getNextContentNode(j, this.element) : g = !0;
                return j && (b.selectNodeContents(j),
                b.collapse(!0)),
                !0
            }
            a.previousSibling && a.previousSibling.nodeType === ice.dom.TEXT_NODE && 0 === a.previousSibling.length && a.parentNode.removeChild(a.previousSibling),
            a.nextSibling && a.nextSibling.nodeType === ice.dom.TEXT_NODE && 0 === a.nextSibling.length && a.parentNode.removeChild(a.nextSibling);
            var k, l = this.getIceNode(a.previousSibling, "deleteType"), m = this.getIceNode(a.nextSibling, "deleteType");
            if (l && this._currentUserIceNode(l)) {
                if (k = l,
                k.appendChild(a),
                m && this._currentUserIceNode(m)) {
                    var n = ice.dom.extractContent(m);
                    ice.dom.append(k, n),
                    m.parentNode.removeChild(m)
                }
            } else
                m && this._currentUserIceNode(m) ? (k = m,
                k.insertBefore(a, k.firstChild)) : (k = this.createIceNode("deleteType"),
                a.parentNode.insertBefore(k, a),
                k.appendChild(a));
            return b && (ice.dom.isStubElement(a) ? b.selectNode(a) : b.selectNodeContents(a),
            c ? b.collapse(!0) : b.collapse(),
            a.normalize()),
            !0
        },
        _handleAncillaryKey: function(a) {
            var b = a.keyCode ? a.keyCode : a.which
              , c = ice.dom.browser()
              , d = !0
              , e = (a.shiftKey,
            this)
              , f = e.getCurrentRange();
            switch (b) {
            case ice.dom.DOM_VK_DELETE:
                d = this.deleteContents(),
                this.pluginsManager.fireKeyPressed(a);
                break;
            case 46:
                d = this.deleteContents(!0),
                this.pluginsManager.fireKeyPressed(a);
                break;
            case ice.dom.DOM_VK_DOWN:
            case ice.dom.DOM_VK_UP:
            case ice.dom.DOM_VK_LEFT:
                this.pluginsManager.fireCaretPositioned(),
                "mozilla" === c.type && (this.visible(f.startContainer) || (f.startContainer.parentNode.previousSibling ? (f.setEnd(f.startContainer.parentNode.previousSibling, 0),
                f.moveEnd(ice.dom.CHARACTER_UNIT, ice.dom.getNodeCharacterLength(f.endContainer)),
                f.collapse(!1)) : (f.setEnd(f.startContainer.parentNode.nextSibling, 0),
                f.collapse(!1)))),
                d = !1;
                break;
            case ice.dom.DOM_VK_RIGHT:
                this.pluginsManager.fireCaretPositioned(),
                "mozilla" === c.type && (this.visible(f.startContainer) || f.startContainer.parentNode.nextSibling && (f.setStart(f.startContainer.parentNode.nextSibling, 0),
                f.collapse(!0))),
                d = !1;
                break;
            case 32:
                d = !0;
                var f = this.getCurrentRange();
                this._moveRangeToValidTrackingPos(f, f.startContainer),
                this.insert("\xa0", f);
                break;
            default:
                d = !1
            }
            return d === !0 ? (ice.dom.preventDefault(a),
            !1) : !0
        },
        keyDown: function(a) {
            if (!this.pluginsManager.fireKeyDown(a))
                return ice.dom.preventDefault(a),
                !1;
            var b = !1;
            if (this._handleSpecialKey(a) === !1)
                return ice.dom.isBrowser("msie") !== !0 && (this._preventKeyPress = !0),
                !1;
            if (!(a.ctrlKey !== !0 && a.metaKey !== !0 || ice.dom.isBrowser("msie") !== !0 && ice.dom.isBrowser("chrome") !== !0 || this.pluginsManager.fireKeyPressed(a)))
                return !1;
            switch (a.keyCode) {
            case 27:
                break;
            default:
                /Firefox/.test(navigator.userAgent) !== !0 && (b = !this._handleAncillaryKey(a))
            }
            return b ? (ice.dom.preventDefault(a),
            !1) : !0
        },
        keyPress: function(a) {
            if (this._preventKeyPress === !0)
                return this._preventKeyPress = !1,
                void 0;
            var b = null;
            if (null == a.which ? b = String.fromCharCode(a.keyCode) : a.which > 0 && (b = String.fromCharCode(a.which)),
            !this.pluginsManager.fireKeyPress(a))
                return !1;
            if (a.ctrlKey || a.metaKey)
                return !0;
            var c = this.getCurrentRange()
              , d = ice.dom.parents(c.startContainer, "br")[0] || null;
            if (d && (c.moveToNextEl(d),
            d.parentNode.removeChild(d)),
            null !== b && a.ctrlKey !== !0 && a.metaKey !== !0) {
                var e = a.keyCode ? a.keyCode : a.which;
                switch (e) {
                case ice.dom.DOM_VK_DELETE:
                    return this._handleAncillaryKey(a);
                case ice.dom.DOM_VK_ENTER:
                    return this._handleEnter();
                case 32:
                    return this._handleAncillaryKey(a);
                default:
                    return this._moveRangeToValidTrackingPos(c, c.startContainer),
                    this.insert()
                }
            }
            return this._handleAncillaryKey(a)
        },
        _handleEnter: function() {
            var a = this.getCurrentRange();
            return a.collapsed || this.deleteContents(),
            !0
        },
        _handleSpecialKey: function(a) {
            var b = a.which;
            null === b && (b = a.keyCode);
            var c = !1;
            switch (b) {
            case 65:
                if (a.ctrlKey === !0 || a.metaKey === !0) {
                    c = !0;
                    var d = this.getCurrentRange();
                    if (ice.dom.isBrowser("msie") === !0) {
                        var e = this.env.document.createTextNode("")
                          , f = this.env.document.createTextNode("");
                        this.element.firstChild ? ice.dom.insertBefore(this.element.firstChild, e) : this.element.appendChild(e),
                        this.element.appendChild(f),
                        d.setStart(e, 0),
                        d.setEnd(f, 0)
                    } else {
                        d.setStart(d.getFirstSelectableChild(this.element), 0);
                        var g = d.getLastSelectableChild(this.element);
                        d.setEnd(g, g.length)
                    }
                    this.selection.addRange(d)
                }
            }
            return c === !0 ? (ice.dom.preventDefault(a),
            !1) : !0
        },
        mouseUp: function(a) {
            return this.pluginsManager.fireClicked(a) ? (this.pluginsManager.fireSelectionChanged(this.getCurrentRange()),
            void 0) : !1
        },
        mouseDown: function(a) {
            return this.pluginsManager.fireMouseDown(a) ? (this.pluginsManager.fireCaretUpdated(),
            void 0) : !1
        }
    },
    c.ice = this.ice || {},
    c.ice.InlineChangeEditor = b
}
.call(this),
function() {
    var a = this
      , b = {};
    b.DOM_VK_DELETE = 8,
    b.DOM_VK_LEFT = 37,
    b.DOM_VK_UP = 38,
    b.DOM_VK_RIGHT = 39,
    b.DOM_VK_DOWN = 40,
    b.DOM_VK_ENTER = 13,
    b.ELEMENT_NODE = 1,
    b.ATTRIBUTE_NODE = 2,
    b.TEXT_NODE = 3,
    b.CDATA_SECTION_NODE = 4,
    b.ENTITY_REFERENCE_NODE = 5,
    b.ENTITY_NODE = 6,
    b.PROCESSING_INSTRUCTION_NODE = 7,
    b.COMMENT_NODE = 8,
    b.DOCUMENT_NODE = 9,
    b.DOCUMENT_TYPE_NODE = 10,
    b.DOCUMENT_FRAGMENT_NODE = 11,
    b.NOTATION_NODE = 12,
    b.CHARACTER_UNIT = "character",
    b.WORD_UNIT = "word",
    b.BREAK_ELEMENT = "br",
    b.CONTENT_STUB_ELEMENTS = ["img", "hr", "iframe", "param", "link", "meta", "input", "frame", "col", "base", "area"],
    b.BLOCK_ELEMENTS = ["p", "div", "pre", "ul", "ol", "li", "table", "tbody", "td", "th", "fieldset", "form", "blockquote", "dl", "dt", "dd", "dir", "center", "address", "h1", "h2", "h3", "h4", "h5", "h6"],
    b.TEXT_CONTAINER_ELEMENTS = ["p", "div", "pre", "li", "td", "th", "blockquote", "dt", "dd", "center", "address", "h1", "h2", "h3", "h4", "h5", "h6"],
    b.STUB_ELEMENTS = b.CONTENT_STUB_ELEMENTS.slice(),
    b.STUB_ELEMENTS.push(b.BREAK_ELEMENT),
    b.getKeyChar = function(a) {
        return String.fromCharCode(a.which)
    }
    ,
    b.getClass = function(a, b, c) {
        return b || (b = document.body),
        a = "." + a.split(" ").join("."),
        c && (a = c + a),
        jQuery.makeArray(jQuery(b).find(a))
    }
    ,
    b.getId = function(a, b) {
        return b || (b = document),
        element = b.getElementById(a)
    }
    ,
    b.getTag = function(a, b) {
        return b || (b = document),
        jQuery.makeArray(jQuery(b).find(a))
    }
    ,
    b.getElementWidth = function(a) {
        return a.offsetWidth
    }
    ,
    b.getElementHeight = function(a) {
        return a.offsetHeight
    }
    ,
    b.getElementDimensions = function(a) {
        var c = {
            width: b.getElementWidth(a),
            height: b.getElementHeight(a)
        };
        return c
    }
    ,
    b.trim = function(a) {
        return jQuery.trim(a)
    }
    ,
    b.empty = function(a) {
        return a ? jQuery(a).empty() : void 0
    }
    ,
    b.remove = function(a) {
        return a ? jQuery(a).remove() : void 0
    }
    ,
    b.prepend = function(a, b) {
        jQuery(a).prepend(b)
    }
    ,
    b.append = function(a, b) {
        jQuery(a).append(b)
    }
    ,
    b.insertBefore = function(a, b) {
        jQuery(a).before(b)
    }
    ,
    b.insertAfter = function(a, b) {
        jQuery(a).after(b)
    }
    ,
    b.getHtml = function(a) {
        return jQuery(a).html()
    }
    ,
    b.setHtml = function(a, b) {
        a && jQuery(a).html(b)
    }
    ,
    b.removeWhitespace = function(a) {
        jQuery(a).contents().filter(function() {
            return this.nodeType != ice.dom.TEXT_NODE && "UL" == this.nodeName || "OL" == this.nodeName ? (b.removeWhitespace(this),
            !1) : this.nodeType != ice.dom.TEXT_NODE ? !1 : !/\S/.test(this.nodeValue)
        }).remove()
    }
    ,
    b.contents = function(a) {
        return jQuery.makeArray(jQuery(a).contents())
    }
    ,
    b.extractContent = function(a) {
        for (var b, c = document.createDocumentFragment(); b = a.firstChild; )
            c.appendChild(b);
        return c
    }
    ,
    b.getNode = function(a, c) {
        return b.is(a, c) ? a : b.parents(a, c)[0] || null
    }
    ,
    b.getParents = function(a, b, c) {
        for (var d = jQuery(a).parents(b), e = d.length, f = [], g = 0; e > g && d[g] !== c; g++)
            f.push(d[g]);
        return f
    }
    ,
    b.hasBlockChildren = function(a) {
        for (var c = a.childNodes.length, d = 0; c > d; d++)
            if (a.childNodes[d].nodeType === b.ELEMENT_NODE && b.isBlockElement(a.childNodes[d]) === !0)
                return !0;
        return !1
    }
    ,
    b.removeTag = function(a, b) {
        return jQuery(a).find(b).replaceWith(function() {
            return jQuery(this).contents()
        }),
        a
    }
    ,
    b.stripEnclosingTags = function(a, b) {
        var c = jQuery(a);
        return c.find("*").not(b).replaceWith(function() {
            var a, b = jQuery();
            try {
                a = jQuery(this),
                b = a.contents()
            } catch (c) {}
            return 0 === b.length && a.remove(),
            b
        }),
        c[0]
    }
    ,
    b.getSiblings = function(a, b, c, d) {
        if (c === !0)
            return "prev" === b ? jQuery(a).prevAll() : jQuery(a).nextAll();
        var e = [];
        if ("prev" === b)
            for (; a.previousSibling && (a = a.previousSibling,
            a !== d); )
                e.push(a);
        else
            for (; a.nextSibling && (a = a.nextSibling,
            a !== d); )
                e.push(a);
        return e
    }
    ,
    b.getNodeTextContent = function(a) {
        return jQuery(a).text()
    }
    ,
    b.getNodeStubContent = function(a) {
        return jQuery(a).find(b.CONTENT_STUB_ELEMENTS.join(", "))
    }
    ,
    b.hasNoTextOrStubContent = function(a) {
        return b.getNodeTextContent(a).length > 0 ? !1 : jQuery(a).find(b.CONTENT_STUB_ELEMENTS.join(", ")).length > 0 ? !1 : !0
    }
    ,
    b.getNodeCharacterLength = function(a) {
        return b.getNodeTextContent(a).length + jQuery(a).find(b.STUB_ELEMENTS.join(", ")).length
    }
    ,
    b.setNodeTextContent = function(a, b) {
        return jQuery(a).text(b)
    }
    ,
    b.getTagName = function(a) {
        return a.tagName && a.tagName.toLowerCase() || null
    }
    ,
    b.getIframeDocument = function(a) {
        var b = null;
        return a.contentDocument ? b = a.contentDocument : a.contentWindow ? b = a.contentWindow.document : a.document && (b = a.document),
        b
    }
    ,
    b.isBlockElement = function(a) {
        return -1 != b.BLOCK_ELEMENTS.lastIndexOf(a.nodeName.toLowerCase())
    }
    ,
    b.isStubElement = function(a) {
        return -1 != b.STUB_ELEMENTS.lastIndexOf(a.nodeName.toLowerCase())
    }
    ,
    b.removeBRFromChild = function(a) {
        if (a && a.hasChildNodes())
            for (var b = 0; b < a.childNodes.length; b++) {
                var c = a.childNodes[b];
                c && ice.dom.BREAK_ELEMENT == ice.dom.getTagName(c) && c.parentNode.removeChild(c)
            }
    }
    ,
    b.isChildOf = function(a, b) {
        try {
            for (; a && a.parentNode; ) {
                if (a.parentNode === b)
                    return !0;
                a = a.parentNode
            }
        } catch (c) {}
        return !1
    }
    ,
    b.isChildOfTagName = function(a, b) {
        try {
            for (; a && a.parentNode; ) {
                if (a.parentNode && a.parentNode.tagName && a.parentNode.tagName.toLowerCase() === b)
                    return a.parentNode;
                a = a.parentNode
            }
        } catch (c) {}
        return !1
    }
    ,
    b.isChildOfTagNames = function(a, b) {
        try {
            for (; a && a.parentNode; ) {
                if (a.parentNode && a.parentNode.tagName) {
                    tagName = a.parentNode.tagName.toLowerCase();
                    for (var c = 0; c < b.length; c++)
                        if (tagName === b[c])
                            return a.parentNode
                }
                a = a.parentNode
            }
        } catch (d) {}
        return null
    }
    ,
    b.isChildOfClassName = function(a, b) {
        try {
            for (; a && a.parentNode; ) {
                if (jQuery(a.parentNode).hasClass(b))
                    return a.parentNode;
                a = a.parentNode
            }
        } catch (c) {}
        return null
    }
    ,
    b.cloneNode = function(a, b) {
        return void 0 === b && (b = !0),
        jQuery(a).clone(b)
    }
    ,
    b.bind = function(a, b, c) {
        return jQuery(a).bind(b, c)
    }
    ,
    b.unbind = function(a, b, c) {
        return jQuery(a).unbind(b, c)
    }
    ,
    b.attr = function(a, b, c) {
        return c ? jQuery(a).attr(b, c) : jQuery(a).attr(b)
    }
    ,
    b.replaceWith = function(a, b) {
        return jQuery(a).replaceWith(b)
    }
    ,
    b.removeAttr = function(a, b) {
        jQuery(a).removeAttr(b)
    }
    ,
    b.getElementsBetween = function(a, c) {
        var d = [];
        if (a === c)
            return d;
        if (b.isChildOf(c, a) === !0) {
            for (var e = a.childNodes.length, f = 0; e > f && a.childNodes[f] !== c; f++) {
                if (b.isChildOf(c, a.childNodes[f]) === !0)
                    return b.arrayMerge(d, b.getElementsBetween(a.childNodes[f], c));
                d.push(a.childNodes[f])
            }
            return d
        }
        for (var g = a.nextSibling; g; ) {
            if (b.isChildOf(c, g) === !0)
                return d = b.arrayMerge(d, b.getElementsBetween(g, c));
            if (g === c)
                return d;
            d.push(g),
            g = g.nextSibling
        }
        for (var h = b.getParents(a), i = b.getParents(c), j = b.arrayDiff(h, i, !0), k = j.length, l = 0; k - 1 > l; l++)
            d = b.arrayMerge(d, b.getSiblings(j[l], "next"));
        var m = j[j.length - 1];
        return d = b.arrayMerge(d, b.getElementsBetween(m, c))
    }
    ,
    b.getCommonAncestor = function(a, c) {
        for (var d = a; d; ) {
            if (b.isChildOf(c, d) === !0)
                return d;
            d = d.parentNode
        }
        return null
    }
    ,
    b.getNextNode = function(a, c) {
        if (a)
            for (; a.parentNode; ) {
                if (a === c)
                    return null;
                if (a.nextSibling) {
                    if (a.nextSibling.nodeType === b.TEXT_NODE && 0 === a.nextSibling.length) {
                        a = a.nextSibling;
                        continue
                    }
                    return b.getFirstChild(a.nextSibling)
                }
                a = a.parentNode
            }
        return null
    }
    ,
    b.getNextContentNode = function(a, c) {
        if (a)
            for (; a.parentNode; ) {
                if (a === c)
                    return null;
                if (a.nextSibling && b.canContainTextElement(b.getBlockParent(a))) {
                    if (a.nextSibling.nodeType === b.TEXT_NODE && 0 === a.nextSibling.length) {
                        a = a.nextSibling;
                        continue
                    }
                    return a.nextSibling
                }
                if (a.nextElementSibling)
                    return a.nextElementSibling;
                a = a.parentNode
            }
        return null
    }
    ,
    b.getPrevNode = function(a, c) {
        if (a)
            for (; a.parentNode; ) {
                if (a === c)
                    return null;
                if (a.previousSibling) {
                    if (a.previousSibling.nodeType === b.TEXT_NODE && 0 === a.previousSibling.length) {
                        a = a.previousSibling;
                        continue
                    }
                    return b.getLastChild(a.previousSibling)
                }
                a = a.parentNode
            }
        return null
    }
    ,
    b.getPrevContentNode = function(a, c) {
        if (a)
            for (; a.parentNode; ) {
                if (a === c)
                    return null;
                if (a.previousSibling && b.canContainTextElement(b.getBlockParent(a))) {
                    if (a.previousSibling.nodeType === b.TEXT_NODE && 0 === a.previousSibling.length) {
                        a = a.previousSibling;
                        continue
                    }
                    return a.previousSibling
                }
                if (a.previousElementSibling)
                    return a.previousElementSibling;
                a = a.parentNode
            }
        return null
    }
    ,
    b.canContainTextElement = function(a) {
        return a && a.nodeName ? -1 != b.TEXT_CONTAINER_ELEMENTS.lastIndexOf(a.nodeName.toLowerCase()) : !1
    }
    ,
    b.getFirstChild = function(a) {
        return a.firstChild ? a.firstChild.nodeType === b.ELEMENT_NODE ? b.getFirstChild(a.firstChild) : a.firstChild : a
    }
    ,
    b.getLastChild = function(a) {
        return a.lastChild ? a.lastChild.nodeType === b.ELEMENT_NODE ? b.getLastChild(a.lastChild) : a.lastChild : a
    }
    ,
    b.removeEmptyNodes = function(a, c) {
        for (var d = jQuery(a).find(":empty"), e = d.length; e > 0; )
            e--,
            b.isStubElement(d[e]) === !1 && (c && c.call(this, d[e]) === !1 || b.remove(d[e]))
    }
    ,
    b.create = function(a) {
        return jQuery(a)[0]
    }
    ,
    b.find = function(a, b) {
        return jQuery(a).find(b)
    }
    ,
    b.children = function(a, b) {
        return jQuery(a).children(b)
    }
    ,
    b.parent = function(a, b) {
        return jQuery(a).parent(b)[0]
    }
    ,
    b.parents = function(a, b) {
        return jQuery(a).parents(b)
    }
    ,
    b.is = function(a, b) {
        return jQuery(a).is(b)
    }
    ,
    b.extend = function() {
        return jQuery.extend.apply(this, arguments)
    }
    ,
    b.walk = function(a, c, d) {
        if (a) {
            d || (d = 0);
            var e = c.call(this, a, d);
            e !== !1 && (a.childNodes && a.childNodes.length > 0 ? b.walk(a.firstChild, c, d + 1) : a.nextSibling ? b.walk(a.nextSibling, c, d) : a.parentNode && a.parentNode.nextSibling && b.walk(a.parentNode.nextSibling, c, d - 1))
        }
    }
    ,
    b.revWalk = function(a, c) {
        if (a) {
            var d = c.call(this, a);
            d !== !1 && (a.childNodes && a.childNodes.length > 0 ? b.walk(a.lastChild, c) : a.previousSibling ? b.walk(a.previousSibling, c) : a.parentNode && a.parentNode.previousSibling && b.walk(a.parentNode.previousSibling, c))
        }
    }
    ,
    b.setStyle = function(a, b, c) {
        a && jQuery(a).css(b, c)
    }
    ,
    b.getStyle = function(a, b) {
        return jQuery(a).css(b)
    }
    ,
    b.hasClass = function(a, b) {
        return jQuery(a).hasClass(b)
    }
    ,
    b.addClass = function(a, b) {
        jQuery(a).addClass(b)
    }
    ,
    b.removeClass = function(a, b) {
        jQuery(a).removeClass(b)
    }
    ,
    b.preventDefault = function(a) {
        a.preventDefault(),
        b.stopPropagation(a)
    }
    ,
    b.stopPropagation = function(a) {
        a.stopPropagation()
    }
    ,
    b.noInclusionInherits = function(a, c) {
        (c instanceof String || "string" == typeof c) && (c = window[c]),
        (a instanceof String || "string" == typeof a) && (a = window[a]);
        var d = function() {};
        if (b.isset(c) === !0)
            for (value in c.prototype)
                a.prototype[value] ? d.prototype[value] = c.prototype[value] : a.prototype[value] = c.prototype[value];
        a.prototype && (d.prototype.constructor = c,
        a.prototype["super"] = new d)
    }
    ,
    b.each = function(a, b) {
        jQuery.each(a, function(a, c) {
            b.call(this, a, c)
        })
    }
    ,
    b.foreach = function(a, b) {
        if (a instanceof Array || a instanceof NodeList || "undefined" != typeof a.length && "undefined" != typeof a.item)
            for (var c = a.length, d = 0; c > d; d++) {
                var e = b.call(this, d, a[d]);
                if (e === !1)
                    break
            }
        else
            for (var f in a)
                if (a.hasOwnProperty(f) === !0) {
                    var e = b.call(this, f);
                    if (e === !1)
                        break
                }
    }
    ,
    b.isBlank = function(a) {
        return !a || /^\s*$/.test(a) ? !0 : !1
    }
    ,
    b.isFn = function(a) {
        return "function" == typeof a ? !0 : !1
    }
    ,
    b.isObj = function(a) {
        return null !== a && "object" == typeof a ? !0 : !1
    }
    ,
    b.isset = function(a) {
        return "undefined" != typeof a && null !== a ? !0 : !1
    }
    ,
    b.isArray = function(a) {
        return jQuery.isArray(a)
    }
    ,
    b.isNumeric = function(a) {
        var b = a.match(/^\d+$/);
        return null !== b ? !0 : !1
    }
    ,
    b.getUniqueId = function() {
        var a = (new Date).getTime()
          , b = Math.ceil(1e6 * Math.random())
          , c = a + "" + b;
        return c.substr(5, 18).replace(/,/, "")
    }
    ,
    b.inArray = function(a, b) {
        for (var c = b.length, d = 0; c > d; d++)
            if (a === b[d])
                return !0;
        return !1
    }
    ,
    b.arrayDiff = function(a, c, d) {
        for (var e = a.length, f = [], g = 0; e > g; g++)
            b.inArray(a[g], c) === !1 && f.push(a[g]);
        if (d !== !0) {
            e = c.length;
            for (var g = 0; e > g; g++)
                b.inArray(c[g], a) === !1 && f.push(c[g])
        }
        return f
    }
    ,
    b.arrayMerge = function(a, b) {
        for (var c = b.length, d = 0; c > d; d++)
            a.push(b[d]);
        return a
    }
    ,
    b.stripTags = function(a, c) {
        if ("string" == typeof c) {
            var d = jQuery("<div>" + a + "</div>");
            return d.find("*").not(c).remove(),
            d.html()
        }
        for (var e, f = new RegExp(/<\/?(\w+)((\s+\w+(\s*=\s*(?:".*?"|'.*?'|[^'">\s]+))?)+\s*|\s*)\/?>/gim), g = a; null != (e = f.exec(a)); )
            (b.isset(c) === !1 || b.inArray(e[1], c) !== !0) && (g = g.replace(e[0], ""));
        return g
    }
    ,
    b.browser = function() {
        var a = {};
        return {type: "webkit"}
    }
    ,
    b.getBrowserType = function() {
        if (null === this._browserType) {
            for (var a = ["msie", "firefox", "chrome", "safari"], b = a.length, c = 0; b > c; c++) {
                var d = new RegExp(a[c],"i");
                if (d.test(navigator.userAgent) === !0)
                    return this._browserType = a[c],
                    this._browserType
            }
            this._browserType = "other"
        }
        return this._browserType
    }
    ,
    b.getWebkitType = function() {
        if ("webkit" !== b.browser().type)
            return console.log("Not a webkit!"),
            !1;
        var a = Object.prototype.toString.call(window.HTMLElement).indexOf("Constructor") > 0;
        return a ? "safari" : "chrome"
    }
    ,
    b.isBrowser = function(a) {
        return b.browser().type === a
    }
    ,
    b.getBlockParent = function(a, c) {
        if (b.isBlockElement(a) === !0)
            return a;
        if (a)
            for (; a.parentNode; ) {
                if (a = a.parentNode,
                a === c)
                    return null;
                if (b.isBlockElement(a) === !0)
                    return a
            }
        return null
    }
    ,
    b.findNodeParent = function(a, c, d) {
        if (a)
            for (; a.parentNode; ) {
                if (a === d)
                    return null;
                if (b.is(a, c) === !0)
                    return a;
                a = a.parentNode
            }
        return null
    }
    ,
    b.onBlockBoundary = function(a, c, d) {
        if (!a || !c)
            return !1;
        var e = b.isChildOfTagNames(a, d) || b.is(a, d.join(", ")) && a || null
          , f = b.isChildOfTagNames(c, d) || b.is(c, d.join(", ")) && c || null;
        return e !== f
    }
    ,
    b.isOnBlockBoundary = function(a, c, d) {
        if (!a || !c)
            return !1;
        var e = b.getBlockParent(a, d) || b.isBlockElement(a, d) && a || null
          , f = b.getBlockParent(c, d) || b.isBlockElement(c, d) && c || null;
        return e !== f
    }
    ,
    b.mergeContainers = function(a, c) {
        if (!a || !c)
            return !1;
        if (a.nodeType === b.TEXT_NODE || b.isStubElement(a))
            c.appendChild(a);
        else if (a.nodeType === b.ELEMENT_NODE) {
            for (; a.firstChild; )
                c.appendChild(a.firstChild);
            b.remove(a)
        }
        return !0
    }
    ,
    b.mergeBlockWithSibling = function(a, c, d) {
        var e = d ? jQuery(c).next().get(0) : jQuery(c).prev().get(0);
        return d ? b.mergeContainers(e, c) : b.mergeContainers(c, e),
        a.collapse(!0),
        !0
    }
    ,
    b.date = function(a, c, d) {
        if (null !== c || !d || (c = b.tsIso8601ToTimestamp(d))) {
            for (var e = new Date(c), f = a.split(""), g = f.length, h = "", i = 0; g > i; i++) {
                var j = ""
                  , k = f[i];
                switch (k) {
                case "D":
                case "l":
                    var l = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
                    j = l[e.getDay()],
                    "D" === k && (j = j.substring(0, 3));
                    break;
                case "F":
                case "m":
                    j = e.getMonth() + 1,
                    10 > j && (j = "0" + j);
                    break;
                case "M":
                    months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
                    j = months[e.getMonth()],
                    "M" === k && (j = j.substring(0, 3));
                    break;
                case "d":
                    j = e.getDate();
                    break;
                case "S":
                    j = b.getOrdinalSuffix(e.getDate());
                    break;
                case "Y":
                    j = e.getFullYear();
                    break;
                case "y":
                    j = e.getFullYear(),
                    j = j.toString().substring(2);
                    break;
                case "H":
                    j = e.getHours();
                    break;
                case "h":
                    j = e.getHours(),
                    0 === j ? j = 12 : j > 12 && (j -= 12);
                    break;
                case "i":
                    j = b.addNumberPadding(e.getMinutes());
                    break;
                case "a":
                    j = "am",
                    e.getHours() >= 12 && (j = "pm");
                    break;
                default:
                    j = k
                }
                h += j
            }
            return h
        }
    }
    ,
    b.getOrdinalSuffix = function(a) {
        var b = ""
          , c = a % 100;
        if (c >= 4 && 20 >= c)
            b = "th";
        else
            switch (a % 10) {
            case 1:
                b = "st";
                break;
            case 2:
                b = "nd";
                break;
            case 3:
                b = "rd";
                break;
            default:
                b = "th"
            }
        return b
    }
    ,
    b.addNumberPadding = function(a) {
        return 10 > a && (a = "0" + a),
        a
    }
    ,
    b.tsIso8601ToTimestamp = function(a) {
        var b = /(\d\d\d\d)(?:-?(\d\d)(?:-?(\d\d)(?:[T ](\d\d)(?::?(\d\d)(?::?(\d\d)(?:\.(\d+))?)?)?(?:Z|(?:([-+])(\d\d)(?::?(\d\d))?)?)?)?)?)?/
          , c = a.match(new RegExp(b));
        if (c) {
            var d = new Date;
            d.setDate(c[3]),
            d.setFullYear(c[1]),
            d.setMonth(c[2] - 1),
            d.setHours(c[4]),
            d.setMinutes(c[5]),
            d.setSeconds(c[6]);
            var e = 60 * c[9];
            "+" === c[8] && (e *= -1),
            e -= d.getTimezoneOffset();
            var f = d.getTime() + 60 * e * 1e3;
            return f
        }
        return null
    }
    ,
    a.dom = b
}
.call(this.ice),
function() {
    var a, b = this;
    a = function(a, b, c) {
        this.env = a,
        this.element = a.element,
        this.selection = this.env.selection,
        c || this.removeBookmarks(this.element);
        var d = b || this.selection.getRangeAt(0);
        b = d.cloneRange();
        {
            var e, f = b.startContainer, g = (b.endContainer,
            b.startOffset);
            b.endOffset
        }
        b.collapse(!1);
        var h = this.env.document.createElement("span");
        h.style.display = "none",
        ice.dom.setHtml(h, "&nbsp;"),
        ice.dom.addClass(h, "iceBookmark iceBookmark_end"),
        h.setAttribute("iceBookmark", "end"),
        b.insertNode(h),
        ice.dom.isChildOf(h, this.element) || this.element.appendChild(h),
        b.setStart(f, g),
        b.collapse(!0);
        var i = this.env.document.createElement("span");
        i.style.display = "none",
        ice.dom.addClass(i, "iceBookmark iceBookmark_start"),
        ice.dom.setHtml(i, "&nbsp;"),
        i.setAttribute("iceBookmark", "start");
        try {
            b.insertNode(i),
            i.previousSibling === h && (e = i,
            i = h,
            h = e)
        } catch (j) {
            ice.dom.insertBefore(h, i)
        }
        ice.dom.isChildOf(i, this.element) === !1 && (this.element.firstChild ? ice.dom.insertBefore(this.element.firstChild, i) : this.element.appendChild(i)),
        h.previousSibling || (e = this.env.document.createTextNode(""),
        ice.dom.insertBefore(h, e)),
        i.nextSibling || (e = this.env.document.createTextNode(""),
        ice.dom.insertAfter(i, e)),
        d.setStart(i.nextSibling, 0),
        d.setEnd(h.previousSibling, h.previousSibling.length || 0),
        this.start = i,
        this.end = h
    }
    ,
    a.prototype = {
        selectBookmark: function() {
            var a = this.selection.getRangeAt(0)
              , b = null
              , c = null
              , d = 0
              , e = null;
            if (this.start.nextSibling === this.end || 0 === ice.dom.getElementsBetween(this.start, this.end).length)
                this.end.nextSibling ? b = ice.dom.getFirstChild(this.end.nextSibling) : this.start.previousSibling ? (b = ice.dom.getFirstChild(this.start.previousSibling),
                b.nodeType === ice.dom.TEXT_NODE && (d = b.length)) : (this.end.parentNode.appendChild(this.env.document.createTextNode("")),
                b = ice.dom.getFirstChild(this.end.nextSibling));
            else {
                if (this.start.nextSibling)
                    b = ice.dom.getFirstChild(this.start.nextSibling);
                else {
                    if (!this.start.previousSibling) {
                        var f = this.env.document.createTextNode("");
                        ice.dom.insertBefore(this.start, f)
                    }
                    b = ice.dom.getLastChild(this.start.previousSibling),
                    d = b.length
                }
                this.end.previousSibling ? c = ice.dom.getLastChild(this.end.previousSibling) : (c = ice.dom.getFirstChild(this.end.nextSibling || this.end),
                e = 0)
            }
            ice.dom.remove([this.start, this.end]),
            null === c ? (a.setEnd(b, d),
            a.collapse(!1)) : (a.setStart(b, d),
            null === e && (e = c.length || 0),
            a.setEnd(c, e));
            try {
                this.selection.addRange(a)
            } catch (g) {}
        },
        getBookmark: function(a, b) {
            var c = ice.dom.getClass("iceBookmark_" + b, a)[0];
            return c
        },
        removeBookmarks: function(a) {
            ice.dom.remove(ice.dom.getClass("iceBookmark", a, "span"))
        }
    },
    b.Bookmark = a
}
.call(this.ice),
function() {
    var a, b = this;
    a = function(a) {
        this._selection = null,
        this.env = a,
        this._initializeRangeLibrary(),
        this._getSelection()
    }
    ,
    a.prototype = {
        _getSelection: function() {
            return this._selection ? this._selection.refresh() : this._selection = this.env.frame ? rangy.getIframeSelection(this.env.frame) : rangy.getSelection(),
            this._selection
        },
        createRange: function() {
            return rangy.createRange(this.env.document)
        },
        getRangeAt: function(a) {
            this._selection.refresh();
            try {
                return this._selection.getRangeAt(a)
            } catch (b) {
                return this._selection = null,
                this._getSelection().getRangeAt(0)
            }
        },
        addRange: function(a) {
            this._selection || (this._selection = this._getSelection()),
            this._selection.setSingleRange(a),
            this._selection.ranges = [a]
        },
        _initializeRangeLibrary: function() {
            var a = this;
            rangy.init(),
            rangy.config.checkSelectionRanges = !1;
            var b = function(a, b, c, d) {
                if (0 !== c)
                    switch (b) {
                    case ice.dom.CHARACTER_UNIT:
                        c > 0 ? a.moveCharRight(d, c) : a.moveCharLeft(d, -1 * c);
                        break;
                    case ice.dom.WORD_UNIT:
                    }
            };
            rangy.rangePrototype.moveStart = function(a, c) {
                b(this, a, c, !0)
            }
            ,
            rangy.rangePrototype.moveEnd = function(a, c) {
                b(this, a, c, !1)
            }
            ,
            rangy.rangePrototype.setRange = function(a, b, c) {
                a ? this.setStart(b, c) : this.setEnd(b, c)
            }
            ,
            rangy.rangePrototype.moveCharLeft = function(a, b) {
                var c, d;
                if (a ? (c = this.startContainer,
                d = this.startOffset) : (c = this.endContainer,
                d = this.endOffset),
                c.nodeType === ice.dom.ELEMENT_NODE)
                    if (c.hasChildNodes()) {
                        for (c = c.childNodes[d],
                        c = this.getPreviousTextNode(c); c && c.nodeType == ice.dom.TEXT_NODE && "" === c.nodeValue; )
                            c = this.getPreviousTextNode(c);
                        d = c.data.length - b
                    } else
                        d = -1 * b;
                else
                    d -= b;
                if (0 > d)
                    for (; 0 > d; ) {
                        var e = [];
                        if (c = this.getPreviousTextNode(c, e),
                        !c)
                            return;
                        c.nodeType !== ice.dom.ELEMENT_NODE && (d += c.data.length)
                    }
                this.setRange(a, c, d)
            }
            ,
            rangy.rangePrototype.moveCharRight = function(a, b) {
                var c, d;
                a ? (c = this.startContainer,
                d = this.startOffset) : (c = this.endContainer,
                d = this.endOffset),
                c.nodeType === ice.dom.ELEMENT_NODE ? (c = c.childNodes[d],
                c.nodeType !== ice.dom.TEXT_NODE && (c = this.getNextTextNode(c)),
                d = b) : d += b;
                var e = d - c.data.length;
                if (e > 0) {
                    for (var f = []; e > 0; )
                        if (c = this.getNextContainer(c, f),
                        c.nodeType !== ice.dom.ELEMENT_NODE) {
                            if (c.data.length >= e)
                                break;
                            c.data.length > 0 && (e -= c.data.length)
                        }
                    d = e
                }
                this.setRange(a, c, d)
            }
            ,
            rangy.rangePrototype.getNextContainer = function(a, b) {
                if (!a)
                    return null;
                for (; a.nextSibling; )
                    if (a = a.nextSibling,
                    a.nodeType !== ice.dom.TEXT_NODE) {
                        var c = this.getFirstSelectableChild(a);
                        if (null !== c)
                            return c
                    } else if (this.isSelectable(a) === !0)
                        return a;
                for (; a && !a.nextSibling; )
                    a = a.parentNode;
                if (!a)
                    return null;
                if (a = a.nextSibling,
                this.isSelectable(a) === !0)
                    return a;
                b && ice.dom.isBlockElement(a) === !0 && b.push(a);
                var d = this.getFirstSelectableChild(a);
                return null !== d ? d : this.getNextContainer(a, b)
            }
            ,
            rangy.rangePrototype.getPreviousContainer = function(a, b) {
                if (!a)
                    return null;
                for (; a.previousSibling; )
                    if (a = a.previousSibling,
                    a.nodeType !== ice.dom.TEXT_NODE) {
                        if (ice.dom.isStubElement(a) === !0)
                            return a;
                        var c = this.getLastSelectableChild(a);
                        if (null !== c)
                            return c
                    } else if (this.isSelectable(a) === !0)
                        return a;
                for (; a && !a.previousSibling; )
                    a = a.parentNode;
                if (!a)
                    return null;
                if (a = a.previousSibling,
                this.isSelectable(a) === !0)
                    return a;
                b && ice.dom.isBlockElement(a) === !0 && b.push(a);
                var d = this.getLastSelectableChild(a);
                return null !== d ? d : this.getPreviousContainer(a, b)
            }
            ,
            rangy.rangePrototype.getNextTextNode = function(a) {
                return a.nodeType === ice.dom.ELEMENT_NODE && 0 !== a.childNodes.length ? this.getFirstSelectableChild(a) : (a = this.getNextContainer(a),
                a.nodeType === ice.dom.TEXT_NODE ? a : this.getNextTextNode(a))
            }
            ,
            rangy.rangePrototype.getPreviousTextNode = function(a, b) {
                return a = this.getPreviousContainer(a, b),
                a.nodeType === ice.dom.TEXT_NODE ? a : this.getPreviousTextNode(a, b)
            }
            ,
            rangy.rangePrototype.getFirstSelectableChild = function(a) {
                if (a) {
                    if (a.nodeType === ice.dom.TEXT_NODE)
                        return a;
                    for (var b = a.firstChild; b; ) {
                        if (this.isSelectable(b) === !0)
                            return b;
                        if (b.firstChild) {
                            var c = this.getFirstSelectableChild(b);
                            if (null !== c)
                                return c;
                            b = b.nextSibling
                        } else
                            b = b.nextSibling
                    }
                }
                return null
            }
            ,
            rangy.rangePrototype.getLastSelectableChild = function(a) {
                if (a) {
                    if (a.nodeType === ice.dom.TEXT_NODE)
                        return a;
                    for (var b = a.lastChild; b; ) {
                        if (this.isSelectable(b) === !0)
                            return b;
                        if (b.lastChild) {
                            var c = this.getLastSelectableChild(b);
                            if (null !== c)
                                return c;
                            b = b.previousSibling
                        } else
                            b = b.previousSibling
                    }
                }
                return null
            }
            ,
            rangy.rangePrototype.isSelectable = function(a) {
                return a && a.nodeType === ice.dom.TEXT_NODE && 0 !== a.data.length ? !0 : !1
            }
            ,
            rangy.rangePrototype.getHTMLContents = function(b) {
                b || (b = this.cloneContents());
                var c = a.env.document.createElement("div");
                return c.appendChild(b.cloneNode(!0)),
                c.innerHTML
            }
            ,
            rangy.rangePrototype.getHTMLContentsObj = function() {
                return this.cloneContents()
            }
        }
    },
    b.Selection = a
}
.call(this.ice),
function() {
    var a = this
      , b = function(a) {
        this._ice = a
    };
    b.prototype = {
        start: function() {},
        clicked: function() {
            return !0
        },
        mouseDown: function() {
            return !0
        },
        keyDown: function() {
            return !0
        },
        keyPress: function() {
            return !0
        },
        selectionChanged: function() {},
        setEnabled: function() {},
        setDisabled: function() {},
        caretUpdated: function() {},
        nodeInserted: function() {},
        nodeCreated: function() {},
        caretPositioned: function() {},
        remove: function() {
            this._ice.removeKeyPressListener(this)
        },
        setSettings: function() {}
    },
    a.IcePlugin = b
}
.call(this.ice),
function() {
    var a = this
      , b = function(a) {
        this.plugins = {},
        this.pluginConstructors = {},
        this.keyPressListeners = {},
        this.activePlugin = null,
        this.pluginSets = {},
        this.activePluginSet = null,
        this._ice = a
    };
    b.prototype = {
        getPluginNames: function() {
            var a = [];
            for (var b in this.plugins)
                a.push(b);
            return a
        },
        addPluginObject: function(a, b) {
            this.plugins[a] = b
        },
        addPlugin: function(a, b) {
            if ("function" != typeof b)
                throw Error("IcePluginException: plugin must be a constructor function");
            ice.dom.isset(this.pluginConstructors[a]) === !1 && (this.pluginConstructors[a] = b)
        },
        loadPlugins: function(a, b) {
            if (0 === a.length)
                b.call(this);
            else {
                var c = a.shift();
                if ("object" == typeof c && (c = c.name),
                ice.dom.isset(ice._plugin[c]) !== !0)
                    throw new Error("plugin was not included in the page: " + c);
                this.addPlugin(c, ice._plugin[c]),
                this.loadPlugins(a, b)
            }
        },
        _enableSet: function(a) {
            this.activePluginSet = a;
            for (var b = this.pluginSets[a].length, c = 0; b > c; c++) {
                var d = this.pluginSets[a][c]
                  , e = "";
                e = "object" == typeof d ? d.name : d;
                var f = this.pluginConstructors[e];
                if (f) {
                    var g = new f(this._ice);
                    this.plugins[e] = g,
                    ice.dom.isset(d.settings) === !0 && g.setSettings(d.settings),
                    g.start()
                }
            }
        },
        setActivePlugin: function(a) {
            this.activePlugin = a
        },
        getActivePlugin: function() {
            return this.activePlugin
        },
        _getPluginName: function(a) {
            var b = a.toString()
              , c = "function ".length
              , d = b.substr(c, b.indexOf("(") - c);
            return d
        },
        removePlugin: function(a) {
            this.plugins[a] && this.plugins[a].remove()
        },
        getPlugin: function(a) {
            return this.plugins[a]
        },
        usePlugins: function(a, b, c) {
            var d = this;
            this.pluginSets[a] = ice.dom.isset(b) === !0 ? b : [];
            var e = this.pluginSets[a].concat([]);
            this.loadPlugins(e, function() {
                d._enableSet(a),
                c && c.call(this)
            })
        },
        disablePlugin: function(a) {
            this.plugins[a].disable()
        },
        isPluginElement: function(a) {
            for (var b in this.plugins)
                if (this.plugins[b].isPluginElement && this.plugins[b].isPluginElement(a) === !0)
                    return !0;
            return !1
        },
        fireKeyPressed: function(a) {
            if (this._fireKeyPressFns(a, "all_keys") === !1)
                return !1;
            var b = [];
            switch ((a.ctrlKey === !0 || a.metaKey === !0) && b.push("ctrl"),
            a.shiftKey === !0 && b.push("shift"),
            a.altKey === !0 && b.push("alt"),
            a.keyCode) {
            case 13:
                b.push("enter");
                break;
            case ice.dom.DOM_VK_LEFT:
                b.push("left");
                break;
            case ice.dom.DOM_VK_RIGHT:
                b.push("right");
                break;
            case ice.dom.DOM_VK_UP:
                b.push("up");
                break;
            case ice.dom.DOM_VK_DOWN:
                b.push("down");
                break;
            case 9:
                b.push("tab");
                break;
            case ice.dom.DOM_VK_DELETE:
                b.push("delete");
                break;
            default:
                var c;
                a.keyCode ? c = a.keyCode : a.which && (c = a.which),
                c && b.push(String.fromCharCode(c).toLowerCase())
            }
            var d = b.sort().join("+");
            return this._fireKeyPressFns(a, d)
        },
        _fireKeyPressFns: function(a, b) {
            if (this.keyPressListeners[b])
                for (var c = this.keyPressListeners[b].length, d = 0; c > d; d++) {
                    var e = this.keyPressListeners[b][d]
                      , f = e.fn
                      , g = e.plugin
                      , h = e.data;
                    if (f)
                        if (ice.dom.isFn(f) === !0) {
                            if (f.call(g, a, h) === !0)
                                return ice.dom.preventDefault(a),
                                !1
                        } else if (g[f] && g[f].call(g, a, h) === !0)
                            return ice.dom.preventDefault(a),
                            !1
                }
            return !0
        },
        fireSelectionChanged: function(a) {
            for (var b in this.plugins)
                this.plugins[b].selectionChanged(a)
        },
        fireNodeInserted: function(a, b) {
            for (var c in this.plugins)
                if (this.plugins[c].nodeInserted(a, b) === !1)
                    return !1
        },
        fireNodeCreated: function(a, b) {
            for (var c in this.plugins)
                if (this.plugins[c].nodeCreated(a, b) === !1)
                    return !1
        },
        fireCaretPositioned: function() {
            for (var a in this.plugins)
                this.plugins[a].caretPositioned()
        },
        fireClicked: function(a) {
            var b = !0;
            for (var c in this.plugins)
                this.plugins[c].clicked(a) === !1 && (b = !1);
            return b
        },
        fireMouseDown: function(a) {
            var b = !0;
            for (var c in this.plugins)
                this.plugins[c].mouseDown(a) === !1 && (b = !1);
            return b
        },
        fireKeyDown: function(a) {
            var b = !0;
            for (var c in this.plugins)
                this.plugins[c].keyDown(a) === !1 && (b = !1);
            return b
        },
        fireKeyPress: function(a) {
            var b = !0;
            for (var c in this.plugins)
                this.plugins[c].keyPress(a) === !1 && (b = !1);
            return b
        },
        fireEnabled: function(a) {
            for (var b in this.plugins)
                this.plugins[b].setEnabled(a)
        },
        fireDisabled: function(a) {
            for (var b in this.plugins)
                this.plugins[b].setDisabled(a)
        },
        fireCaretUpdated: function() {
            for (var a in this.plugins)
                this.plugins[a].caretUpdated && this.plugins[a].caretUpdated()
        }
    },
    a._plugin = {},
    a.IcePluginManager = b
}
.call(this.ice),
function() {
    var a, b = this;
    a = function(a) {
        this._ice = a
    }
    ,
    a.prototype = {
        nodeCreated: function(a, b) {
            a.setAttribute("title", (b.action || "Modified") + " by " + a.getAttribute(this._ice.userNameAttribute) + " - " + ice.dom.date("m/d/Y h:ia", parseInt(a.getAttribute(this._ice.timeAttribute))))
        }
    },
    ice.dom.noInclusionInherits(a, ice.IcePlugin),
    b._plugin.IceAddTitlePlugin = a
}
.call(this.ice),
function() {
    var a, b = this;
    a = function(a) {
        this._ice = a,
        this._tmpNode = null,
        this._tmpNodeTagName = "icepaste",
        this._pasteId = "icepastediv";
        var b = this;
        this.pasteType = "formattedClean",
        this.preserve = "p",
        this.beforePasteClean = function(a) {
            return a
        }
        ,
        this.afterPasteClean = function(a) {
            return a
        }
        ,
        a.element.oncopy = function() {
            return b.handleCopy.apply(b)
        }
    }
    ,
    a.prototype = {
        setSettings: function(a) {
            a = a || {},
            ice.dom.extend(this, a),
            this.preserve += "," + this._tmpNodeTagName,
            this.setupPreserved()
        },
        keyDown: function(a) {
            return a.metaKey === !0 || a.ctrlKey === !0 ? (86 == a.keyCode ? this.handlePaste() : 88 == a.keyCode && this.handleCut(),
            !0) : void 0
        },
        keyPress: function(a) {
            var b = null;
            null == a.which ? b = String.fromCharCode(a.keyCode) : a.which > 0 && (b = String.fromCharCode(a.which));
            var c = this;
            return this.cutElement && "x" === b ? ice.dom.isBrowser("webkit") && c.cutElement.focus() : "v" === b && ice.dom.isBrowser("webkit") && this._ice.env.document.getElementById(c._pasteId).focus(),
            !0
        },
        handleCopy: function() {},
        handlePaste: function() {
            var a = this._ice.getCurrentRange();
            if (a.collapsed || (this._ice.isTracking ? (this._ice.deleteContents(),
            a = a.cloneRange()) : (a.deleteContents(),
            a.collapse(!0))),
            this._ice.isTracking && this._ice._moveRangeToValidTrackingPos(a),
            a.startContainer == this._ice.element) {
                var b = ice.dom.find(this._ice.element, this._ice.blockEl)[0];
                b || (b = ice.dom.create("<" + this._ice.blockEl + " ><br/></" + this._ice.blockEl + ">"),
                this._ice.element.appendChild(b)),
                a.setStart(b, 0),
                a.collapse(!0),
                this._ice.env.selection.addRange(a)
            }
            switch (this._tmpNode = this._ice.env.document.createElement(this._tmpNodeTagName),
            a.insertNode(this._tmpNode),
            this.pasteType) {
            case "formatted":
                this.setupPaste();
                break;
            case "formattedClean":
                this.setupPaste(!0)
            }
            return !0
        },
        setupPaste: function(a) {
            var b = this.createDiv(this._pasteId)
              , c = this
              , d = this._ice.getCurrentRange();
            return d.selectNodeContents(b),
            this._ice.selection.addRange(d),
            b.onpaste = function(b) {
                setTimeout(function() {
                    c.handlePasteValue(a)
                }, 0),
                b.stopPropagation()
            }
            ,
            b.focus(),
            !0
        },
        handlePasteValue: function(a) {
            var b = this._ice.env.document
              , c = b.getElementById(this._pasteId)
              , d = ice.dom.getHtml(c)
              , e = ice.dom.children("<div>" + d + "</div>", this._ice.blockEl);
            1 === e.length && ice.dom.getNodeTextContent("<div>" + d + "</div>") === ice.dom.getNodeTextContent(e) && (d = ice.dom.getHtml(d)),
            d = this.beforePasteClean.call(this, d),
            a && (d = this._ice.getCleanContent(d),
            d = this.stripPaste(d)),
            d = this.afterPasteClean.call(this, d),
            d = ice.dom.trim(d);
            var f = this._ice.getCurrentRange();
            f.setStartAfter(this._tmpNode),
            f.collapse(!0);
            var g = null
              , h = null
              , i = null
              , j = f.createContextualFragment(d)
              , k = this._ice.startBatchChange();
            if (ice.dom.hasBlockChildren(j)) {
                var l = ice.dom.isChildOfTagName(this._tmpNode, this._ice.blockEl);
                f.setEndAfter(l.lastChild),
                this._ice.selection.addRange(f);
                var m = f.extractContents()
                  , n = b.createElement(this._ice.blockEl);
                n.appendChild(m),
                ice.dom.insertAfter(l, n),
                f.setStart(n, 0),
                f.collapse(!0),
                this._ice.selection.addRange(f);
                for (var o = f.startContainer; j.firstChild; )
                    if (3 !== j.firstChild.nodeType || jQuery.trim(j.firstChild.nodeValue))
                        if (ice.dom.isBlockElement(j.firstChild)) {
                            if ("" !== j.firstChild.textContent) {
                                g = null;
                                var p = null;
                                this._ice.isTracking ? (p = this._ice.createIceNode("insertType"),
                                this._ice.addChange("insertType", [p]),
                                i = b.createElement(j.firstChild.tagName),
                                p.innerHTML = j.firstChild.innerHTML,
                                i.appendChild(p)) : (p = i = b.createElement(j.firstChild.tagName),
                                i.innerHTML = j.firstChild.innerHTML),
                                h = p,
                                ice.dom.insertBefore(o, i)
                            }
                            j.removeChild(j.firstChild)
                        } else
                            g || (i = b.createElement(this._ice.blockEl),
                            ice.dom.insertBefore(o, i),
                            this._ice.isTracking ? (g = this._ice.createIceNode("insertType"),
                            this._ice.addChange("insertType", [g]),
                            i.appendChild(g)) : g = i),
                            h = g,
                            g.appendChild(j.removeChild(j.firstChild));
                    else
                        j.removeChild(j.firstChild);
                n.textContent || n.parentNode.removeChild(n)
            } else if (this._ice.isTracking)
                i = this._ice.createIceNode("insertType", j),
                this._ice.addChange("insertType", [i]),
                f.insertNode(i),
                h = i;
            else
                for (var q; q = j.firstChild; )
                    f.insertNode(q),
                    f.setStartAfter(q),
                    f.collapse(!0),
                    h = q;
            this._ice.endBatchChange(k),
            c.parentNode.removeChild(c),
            this._cleanup(h)
        },
        createDiv: function(a) {
            var b = this._ice.env.document
              , c = b.getElementById(a);
            c && ice.dom.remove(c);
            var d = b.createElement("div");
            return d.id = a,
            d.setAttribute("contentEditable", !0),
            ice.dom.setStyle(d, "width", "1px"),
            ice.dom.setStyle(d, "height", "1px"),
            ice.dom.setStyle(d, "overflow", "hidden"),
            ice.dom.setStyle(d, "position", "fixed"),
            ice.dom.setStyle(d, "top", "10px"),
            ice.dom.setStyle(d, "left", "10px"),
            d.appendChild(b.createElement("br")),
            b.body.appendChild(d),
            d
        },
        handleCut: function() {
            var a = this
              , b = this._ice.getCurrentRange();
            if (!b.collapsed) {
                this.cutElement = this.createDiv("icecut"),
                this.cutElement.innerHTML = b.getHTMLContents().replace(/ </g, "&nbsp;<").replace(/> /g, ">&nbsp;"),
                this._ice.isTracking ? this._ice.deleteContents() : b.deleteContents();
                var c = this._ice.env.document.createRange();
                c.setStart(this.cutElement.firstChild, 0),
                c.setEndAfter(this.cutElement.lastChild),
                setTimeout(function() {
                    a.cutElement.focus(),
                    setTimeout(function() {
                        ice.dom.remove(a.cutElement),
                        b.setStart(b.startContainer, b.startOffset),
                        b.collapse(!1),
                        a._ice.env.selection.addRange(b)
                    }, 100)
                }, 0),
                a._ice.env.selection.addRange(c)
            }
        },
        stripPaste: function(a) {
            return a = this._cleanWordPaste(a),
            a = this.cleanPreserved(a)
        },
        setupPreserved: function() {
            var a = this;
            this._tags = "",
            this._attributesMap = [],
            ice.dom.each(this.preserve.split(","), function(b, c) {
                c.match(/(\w+)(\[(.+)\])?/);
                var d = RegExp.$1
                  , e = RegExp.$3;
                a._tags && (a._tags += ","),
                a._tags += d.toLowerCase(),
                a._attributesMap[d] = e.split("|")
            })
        },
        cleanPreserved: function(a) {
            var b = this
              , c = this._ice.env.document.createElement("div");
            return c.innerHTML = a,
            c = ice.dom.stripEnclosingTags(c, this._tags),
            ice.dom.each(ice.dom.find(c, this._tags), function(a, c) {
                if (ice.dom.hasClass(c, "skip-clean"))
                    return !0;
                var d = c.tagName.toLowerCase()
                  , e = b._attributesMap[d];
                if (e[0] && "*" === e[0])
                    return !0;
                if (c.hasAttributes())
                    for (var f = c.attributes, a = f.length - 1; a >= 0; a--)
                        ice.dom.inArray(f[a].name, e) || c.removeAttribute(f[a].name)
            }),
            c.innerHTML
        },
        _cleanWordPaste: function(a) {
            return a = a.replace(/<(meta|link)[^>]+>/g, ""),
            a = a.replace(/<!--(.|\s)*?-->/g, ""),
            a = a.replace(/<style>[\s\S]*?<\/style>/g, ""),
            a = a.replace(/<\/?\w+:[^>]*>/gi, ""),
            a = a.replace(/<\\?\?xml[^>]*>/gi, ""),
            a = this._cleanPaste(a),
            a = a.replace(/<(\w[^>]*) (lang)=([^ |>]*)([^>]*)/gi, "<$1$4")
        },
        _cleanPaste: function(a) {
            return a = a.replace(/<b(\s+|>)/g, "<strong$1"),
            a = a.replace(/<\/b(\s+|>)/g, "</strong$1"),
            a = a.replace(/<i(\s+|>)/g, "<em$1"),
            a = a.replace(/<\/i(\s+|>)/g, "</em$1")
        },
        _cleanup: function(a) {
            try {
                a = a && a.lastChild || a || this._tmpNode;
                var b = this._ice.getCurrentRange();
                b.setStartAfter(a),
                b.collapse(!0),
                this._ice.selection.addRange(b),
                this._ice.env.frame ? this._ice.env.frame.contentWindow.focus() : this._ice.element.focus(),
                this._tmpNode.parentNode.removeChild(this._tmpNode),
                this._tmpNode = null;
                for (var c = this._ice.env.document.getElementsByClassName(this._ice.changeTypes.insertType.alias), d = 0; d < c.length; d++)
                    c[d].textContent || c[d].parentNode && c[d].parentNode.removeChild(c[d])
            } catch (e) {
                window.console && console.error(e)
            }
        }
    },
    ice.dom.noInclusionInherits(a, ice.IcePlugin),
    b._plugin.IceCopyPastePlugin = a
}
.call(this.ice),
function() {
    var a = this
      , b = this.ice
      , c = function(a) {
        this._ice = a
    };
    c.prototype = {
        convert: function(a) {
            var c = this;
            try {
                c._ice.placeholdDeletes(),
                b.dom.each(a.getElementsByTagName(this._ice.blockEl), function(a, b) {
                    c._convertBlock(b)
                })
            } catch (d) {
                window.console && console.error(d)
            } finally {
                c._ice.revertDeletePlaceholders()
            }
        },
        _convertBlock: function(a) {
            if (!(b.dom.getNodeTextContent(a) < 2)) {
                var c, d, e, f, g, h = "'", i = '"', j = String.fromCharCode(8216), k = String.fromCharCode(8217), l = String.fromCharCode(8220), m = String.fromCharCode(8221), n = function(a) {
                    return /\d/.test(a)
                }, o = function(a) {
                    return /\w/.test(a)
                }, p = function(a) {
                    return a === String.fromCharCode(160) || a === String.fromCharCode(32)
                }, q = function(a) {
                    return p(a) || "(" === a
                }, r = function(a) {
                    return p(a) || null == a || ";" === a || ")" === a || "." == a || "!" === a || "," === a || "?" === a || ":" === a
                }, s = function(a) {
                    return !p(a)
                }, t = function(a) {
                    return a === h || a === j || a === k
                };
                f = b.dom.getHtml(a).match(/(<("[^"]*"|'[^']*'|[^'">])*>|&.*;|.)/g),
                g = function(a, b, c) {
                    var d = a.length
                      , e = 0 > c ? -1 : 1;
                    return function f(a, b, c) {
                        if (0 > b || b >= d)
                            return null;
                        var g = a[b + e];
                        return g && 1 == g.length && (c += -1 * e,
                        !c) ? g : f(a, b + e, c)
                    }(a, b, c)
                }
                ,
                b.dom.each(f, function(a, b) {
                    if ("&nbsp;" == b && (b = f[a] = " "),
                    1 == b.length) {
                        switch (c = g(f, a, -1),
                        d = b,
                        e = g(f, a, 1),
                        d) {
                        case j:
                        case k:
                            d = h;
                        case h:
                            (null == c || p(c)) && n(e) && n(g(f, a, 2)) && r(g(f, a, 3)) ? d = k : null == c || q(c) && s(e) ? d = j : null == e || s(c) && r(e) ? d = k : o(c) && o(e) && (d = k);
                            break;
                        case l:
                        case m:
                            d = i;
                        case i:
                            r(e) && p(c) && t(g(f, a, -2)) ? d = m : null == c || q(c) && s(e) ? d = l : null == e || s(c) && r(e) ? d = m : (null == c || p(c)) && p(e) && t(g(f, a, 1)) && (d = l)
                        }
                        null != d && (f[a] = d)
                    }
                }),
                b.dom.setHtml(a, f.join(""))
            }
        }
    },
    b.dom.noInclusionInherits(c, b.IcePlugin),
    a.ice._plugin.IceSmartQuotesPlugin = c
}
.call(this),
function() {
    var a = this
      , b = function(a) {
        this._ice = a
    };
    b.prototype = {
        keyDown: function(a) {
            if (ice.dom.isBrowser("mozilla")) {
                var b = parseInt(ice.dom.browser().version);
                if (b > 14 && 173 === a.keyCode || 14 >= b && 109 === a.keyCode)
                    return this.convertEmdash(a)
            } else if (189 === a.keyCode)
                return this.convertEmdash(a);
            return !0
        },
        convertEmdash: function() {
            var a = this._ice.getCurrentRange();
            if (a.collapsed) {
                try {
                    a.moveStart(ice.dom.CHARACTER_UNIT, -1);
                    var b = ice.dom.getParents(a.startContainer, this._ice.blockEl)[0]
                      , d = ice.dom.getParents(a.endContainer, this._ice.blockEl)[0];
                    if (b === d && !this._ice.getIceNode(a.startContainer, "deleteType") && (c = a.toHtml(),
                    "-" === c)) {
                        a.extractContents(),
                        a.collapse();
                        var e = this._ice.env.document.createTextNode("\u2014");
                        return this._ice.isTracking ? this._ice._insertNode(e, a) : (a.insertNode(e),
                        a.setStart(e, 1),
                        a.collapse(!0)),
                        this._ice._preventKeyPress = !0,
                        !1
                    }
                } catch (f) {}
                a.collapse()
            }
            return !0
        }
    },
    ice.dom.noInclusionInherits(b, ice.IcePlugin),
    a._plugin.IceEmdashPlugin = b
}
.call(this.ice);
