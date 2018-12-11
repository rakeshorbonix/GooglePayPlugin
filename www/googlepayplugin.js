// Empty constructor
function GooglePayPlugin() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
// GooglePayPlugin.prototype.show = function(message, duration, successCallback, errorCallback) {
//   var options = {};
//   options.message = message;
//   options.duration = duration;
//   cordova.exec(successCallback, errorCallback, 'GooglePayPlugin', 'show', [options]);
// }
GooglePayPlugin.prototype.show = function(totalPrice, currencyCode,successCallback, errorCallback) {
  var options = {};
  options.totalPrice = totalPrice;
  options.currencyCode = currencyCode;
  cordova.exec(successCallback,errorCallback, 'GooglePayPlugin', 'show',[options]);

}
// function onSuccess(message){
//      console.log(message);
// }





// Installation constructor that binds ToastyPlugin to window
GooglePayPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.googlePayPlugin = new GooglePayPlugin();
  return window.plugins.googlePayPlugin;
};
cordova.addConstructor(GooglePayPlugin.install);
