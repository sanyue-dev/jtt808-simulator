(function () {

    var _seq = 0;

    function AutoCar(map, latlng, opts) {
        this._id = ++_seq;
        this._map = map;
        this._opts = opts || {};
        this._lastPos = latlng;
        this._angle = 0;
        this._visible = true;
        this._marker = null;
        this._label = null;
    }

    AutoCar.prototype.show = function () {
        if (this._marker) return;
        var pos = this._lastPos;
        var icon = this._createIcon();
        this._marker = L.marker(pos, { icon: icon, rotationAngle: 0 });
        this._marker.addTo(this._map);

        if (this._opts.label) {
            this._setLabel(this._opts.label, this._opts.labelColor);
        }
    };

    AutoCar.prototype.moveTo = function (latlng, arrivalTime, direction) {
        if (!this._marker) return;

        this._lastPos = latlng;
        this._marker.setLatLng(latlng);

        if (this._opts.enableRotation !== false && direction != null && direction >= 0) {
            var imgEl = this._marker._icon ? this._marker._icon.querySelector('img') : null;
            if (imgEl) {
                imgEl.style.transform = 'rotate(' + (direction + 90) + 'deg)';
            }
        }

        if (this._opts.autoView) {
            var bounds = this._map.getBounds();
            if (!bounds.contains(latlng)) {
                this._map.setView(latlng, this._map.getZoom());
            }
        }
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
        var el = this._marker.getTooltip();
        if (el && el._container) {
            el._container.style.backgroundColor = color || '#00537f';
            el._container.style.color = '#efefef';
            el._container.style.border = 'none';
            el._container.style.padding = '2px 10px';
            el._container.style.textAlign = 'center';
        }
    };

    window.LeafletAutoCar = AutoCar;

})();
