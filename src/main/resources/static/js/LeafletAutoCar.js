(function () {

    var _seq = 0;

    function AutoCar(map, latlng, opts) {
        this._id = ++_seq;
        this._map = map;
        this._opts = opts || {};
        this._path = [latlng];
        this._angle = 0;
        this._visible = true;
        this._marker = null;
        this._label = null;
        this._animating = false;
    }

    AutoCar.prototype.show = function () {
        if (this._marker) return;
        var pos = this._path[0];
        var icon = this._createIcon();
        this._marker = L.marker(pos, { icon: icon, rotationAngle: 0 });
        this._marker.addTo(this._map);

        if (this._opts.label) {
            this._setLabel(this._opts.label, this._opts.labelColor);
        }
    };

    AutoCar.prototype.moveTo = function (latlng, arrivalTime) {
        this._path.push(latlng);
        this._moveNext();
    };

    AutoCar.prototype.setVisibility = function (visible) {
        this._visible = visible;
        if (this._marker) {
            if (visible) {
                if (!this._map.hasLayer(this._marker)) this._marker.addTo(this._map);
            } else {
                this._map.removeLayer(this._marker);
            }
        }
    };

    AutoCar.prototype.getVisiblity = function () {
        return this._visible;
    };

    AutoCar.prototype.setPosition = function (latlng) {
        if (this._marker) this._marker.setLatLng(latlng);
    };

    AutoCar.prototype.setLabel = function (text, color) {
        this._opts.label = text;
        if (color) this._opts.labelColor = color;
        this._setLabel(text, color);
    };

    AutoCar.prototype._createIcon = function () {
        var iconUrl = '/static/img/vehicle.png';
        var size = 68;
        return L.divIcon({
            className: 'autocar-icon',
            html: '<img src="' + iconUrl + '" style="width:' + size + 'px;height:' + size + 'px;" />',
            iconSize: [size, size],
            iconAnchor: [size / 2, size / 2]
        });
    };

    AutoCar.prototype._setLabel = function (text, color) {
        if (!this._marker) return;
        if (this._label) {
            this._marker.unbindTooltip();
        }
        this._label = true;
        this._marker.bindTooltip(text, {
            permanent: true,
            direction: 'top',
            offset: [0, -38],
            className: 'autocar-label'
        });
        // style the label after binding
        var el = this._marker.getTooltip();
        if (el && el._container) {
            el._container.style.backgroundColor = color || '#00537f';
            el._container.style.color = '#efefef';
            el._container.style.border = 'none';
            el._container.style.padding = '2px 10px';
            el._container.style.textAlign = 'center';
        }
    };

    AutoCar.prototype._moveNext = function () {
        if (this._path.length < 2 || this._animating) return;
        var from = this._path.shift();
        var to = this._path[0];
        this._animateMove(from, to);
    };

    AutoCar.prototype._animateMove = function (from, to) {
        var self = this;
        var marker = this._marker;
        if (!marker) return;

        this._animating = true;

        var p1 = this._map.latLngToContainerPoint(from);
        var p2 = this._map.latLngToContainerPoint(to);
        var dx = p2.x - p1.x;
        var dy = p2.y - p1.y;
        var pixelDist = Math.sqrt(dx * dx + dy * dy);

        // calculate angle for rotation
        var angle = 0;
        if (pixelDist > 0) {
            angle = Math.atan2(dx, -dy) * 180 / Math.PI + 90;
        }

        // animation timing
        var duration = 2000; // default 2s
        if (to.arrivalTime && from.arrivalTime) {
            var dt = to.arrivalTime - from.arrivalTime;
            if (dt > 0 && dt < 30000) duration = dt;
        }
        var frames = Math.max(1, Math.round(pixelDist / 2));
        if (frames > 300) frames = 300;

        var startTime = null;

        function step(timestamp) {
            if (!startTime) startTime = timestamp;
            var elapsed = timestamp - startTime;
            var progress = Math.min(elapsed / duration, 1);

            var lat = from.lat + (to.lat - from.lat) * progress;
            var lng = from.lng + (to.lng - from.lng) * progress;
            var latlng = L.latLng(lat, lng);

            marker.setLatLng(latlng);

            // update rotation
            if (self._opts.enableRotation !== false && pixelDist > 1) {
                var imgEl = marker._icon ? marker._icon.querySelector('img') : null;
                if (imgEl) {
                    imgEl.style.transform = 'rotate(' + angle + 'deg)';
                }
            }

            // auto follow
            if (self._opts.autoView) {
                var bounds = self._map.getBounds();
                if (!bounds.contains(latlng)) {
                    self._map.setView(latlng, self._map.getZoom());
                }
            }

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                self._animating = false;
                self._moveNext();
            }
        }

        // set initial rotation
        if (this._opts.enableRotation !== false && pixelDist > 1) {
            var imgEl = marker._icon ? marker._icon.querySelector('img') : null;
            if (imgEl) {
                imgEl.style.transform = 'rotate(' + angle + 'deg)';
            }
        }

        requestAnimationFrame(step);
    };

    window.LeafletAutoCar = AutoCar;

})();
