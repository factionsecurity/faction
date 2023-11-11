/*
 * jQuery Loading v0.1.0
 * Author: trungk18, tunghoang
 * Licensed under the MIT license
 */
(function ($) {
  var pluginName = "loading";

  // constructor
  function Loading(elem, options) {
    var defaults = {
      overlay: false, // add an overlay background
      width: null, // set fixed width to loading indicator, otherwise calculated relative to element
      indicatorHtml: "<div class='js-loading-indicator' style='display: none;'></div>",
      overlayHtml: "<div class='js-loading-overlay' style='display: none;'></div>",
      base: 0.9, // indicator's width/height relative to element
      circles: 3, // number of indicator circles: maximum is 3
      top: null, //indicator position relative to the top of its container
      left: null, //indicator position relative to the left of its container
      hide: false, // hide the indicator of the current element
      destroy: false //remove the indicator from the DOM
    };

    var elem = elem;
    var $elem = $(elem);
    var options = $.extend({}, defaults, options);
    var originalPosition, $indicator, $overlay;

    function init() {
      // store this element's original position
      originalPosition = $elem.css("position");
      if (originalPosition == "static") {
        $elem.css("position", "relative");
      }
      $elem.addClass("js-loading");

      var size, top, left;
      var elemWidth = $elem.width();
      var elemHeight = $elem.height();

      if (options.width != null) {
        size = options.width;
      } else {
        size = (elemWidth >= elemHeight ? elemHeight : elemWidth) * options.base;
      }

      top = options.top != null ? options.top : (elemHeight - size) / 2;
      left = options.left != null ? options.left : (elemWidth - size) / 2;

      $indicator = $(options.indicatorHtml);
      $indicator.css({ width: size, height: size, top: top, left: left });

      if (options.circles == 2) {
        $indicator.addClass("double");
      } else if (options.circles == 3) {
        $indicator.addClass("triple");
      }

      $elem.append($indicator);

      // add overlay background
      if (options.overlay) {
        $overlay = $(options.overlayHtml);
        $elem.append($overlay);
      }
    }

    function option(key, val) {
      if (val) {
        options[key] = val;
      } else {
        return options[key];
      }
    }

    function destroy() {
      $elem.each(function () {
        var el = this;
        var $el = $(this);

        $indicator && $indicator.remove();
        $overlay && $overlay.remove();
        // rollback original position
        $elem.css("position", originalPosition);
        $elem.removeClass("js-loading");
        $el.removeData('plugin_' + pluginName);
      });
    }

    function show() {
      $indicator && $indicator.fadeIn();
      $overlay && $overlay.fadeIn();
    }

    function hide() {
      $indicator && $indicator.fadeOut();
      $overlay && $overlay.fadeOut();
    }

    init();
    show();

    return {
      option: option,
      destroy: destroy,
      show: show,
      hide: hide
    };
  };

  $.fn[pluginName] = function (options) {
    // If the first parameter is a string, treat this as a call to
    // a public method.
    if (typeof arguments[0] === 'string') {
      var methodName = arguments[0];
      var args = Array.prototype.slice.call(arguments, 1);
      var returnVal;
      this.each(function () {
        // Check that the element has a plugin instance, and that
        // the requested public method exists.
        if (!$.data(this, 'plugin_' + pluginName)) {
          return;
        }
        if (typeof $.data(this, 'plugin_' + pluginName)[methodName] === 'function') {
          // Call the method of the Plugin instance, and Pass it
          // the supplied arguments.
          returnVal = $.data(this, 'plugin_' + pluginName)[methodName].apply(this, args);
        } else {
          throw new Error('Method ' + methodName + ' does not exist on jQuery.' + pluginName);
        }
      });
      if (returnVal !== undefined) {
        // If the method returned a value, return the value.
        return returnVal;
      } else {
        // Otherwise, returning 'this' preserves chainability.
        return this;
      }
      // If the first parameter is an object (options), or was omitted,
      // instantiate a new instance of the plugin.
    } else if (typeof options === "object" || !options) {
      return this.each(function () {
        // Only allow the plugin to be instantiated once.
        var plugin = $.data(this, 'plugin_' + pluginName);
        if (!plugin) {
          // Pass options to Plugin constructor, and store Plugin
          // instance in the elements jQuery data object.
          var loading = new Loading(this, options);
          $.data(this, 'plugin_' + pluginName, loading);
          return loading;
        }
        else {
          if (options.hide)
            plugin.hide();
          if (options.destroy)
            plugin.destroy();
        }
      });
    }
  };

})(jQuery);