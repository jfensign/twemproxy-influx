'use strict';

app = angular.module 'TwemproxyMonit',[
  'ngRoute'
  'ngResource'
  'Controllers'
  'Services'
  'ui.bootstrap'
  'ui.grid' ]

app.config ['$routeProvider', ($routes) ->
  $routes
    .when '/',
      redirectTo: '/clusters'
    .when '/clusters',
      controller: 'ClusterListCtrl',
      templateUrl: '/public/html/partials/cluster-list.html'
    .when '/clusters/:cluster_name',
      controller: 'ClusterSelectCtrl',
      templateUrl: '/public/html/partials/cluster-select.html'
    .otherwise
      redirectTo: '/' ]