Services = angular.module 'Services', []

Services.factory "Configuration", [
  "$resource"
  "$http"
  "$log"
  ($resource, $http, $log) ->
    list: ->
      uri = "/configuration"
      $http.get uri ]

Services.factory "Stats", [
  "$resource"
  "$http"
  "$log"
  ($resource, $http, $log) ->
    list: ->
      uri = "/stats"
      $http.get uri ]