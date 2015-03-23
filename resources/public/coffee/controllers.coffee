Controllers = angular.module 'Controllers', []

Controllers.controller 'ClusterListCtrl', [
  '$scope'
  '$rootScope'
  '$location'
  '$log'
  'Configuration'
  'Stats'
  ($scope, $rootScope, $location, $log, $cluster_configuration, $cluster_stats) -> 

    $scope.errors = []
    $scope.cluster_configuration
    $scope.cluster_stats
    $scope.gridApi
    $scope.cluster_list_grid_configuration

    cluster_stats_query

    $scope.cluster_list_grid_configuration = 
      showFooter: true
      showGroupPanel: true
      showFilter: false
      columnDefs: [
        {displayName: 'Cluster', field: 'cluster', name: 'cluster', width: '10%', groupable: true, cellTemplate: '/public/html/partials/cluser-name-cell.html'}
        {displayName: 'Client Connections', field: 'client_connections', width: '5%'}
        {displayName: 'Client Errors', field: 'client_err', width: '5%'}
        {displayName: 'Forward Errors', field: 'forward_error', width: '5%'}
        {displayName: 'Fragments', field: 'fragments', width: '5%'}
        {displayName: 'Ejections', field: 'server_ejects', width: '5%'}
        {displayName: 'Client EOF', field: 'client_eof', width: '5%'}
        {displayName: 'Cluster Nodes', field: 'servers', width: '20%', cellTemplate: '/public/html/partials/node-cell.html'}
      ]
      groups: ['cluster']
      onRegisterApi: (gridApi) ->
        $scope.gridApi = gridApi

    cluster_stats_query = do $cluster_stats.list

    cluster_stats_query.success (stats) ->
      $scope.cluster_stats = stats
      $scope.cluster_list_grid_configuration.data = (((cluster,stat) ->
        _.extend {
          cluster: cluster, 
          servers: (node for node, node_stats of stat when _.isObject node_stats).join ', '
        }, stat if _.isObject stat) cluster,stat for cluster, stat of stats when _.isObject stat)

    cluster_stats_query.error (e) ->
      $scope.errors.push e

]


Controllers.controller 'ClusterSelectCtrl', [
  '$scope'
  '$rootScope'
  '$location'
  '$log'
  '$routeParams',
  'Configuration'
  'Stats'
  ($scope, $rootScope, $location, $log, $routeParams, $cluster_configuration, $cluster_stats) -> 

    $scope.errors = []
    $scope.cluster_configuration
    $scope.cluster_stats
    $scope.cluster_config
    $scope.gridApi
    $scope.selected_cluster
    $scope.cluster_select_grid_configuration

    cluster_stats_query
    cluster_config_query

    $scope.selected_cluster = $routeParams.cluster_name

    cluster_stats_query  = do $cluster_stats.list
    cluster_config_query = do $cluster_configuration.list

    cluster_stats_query.success (stats) ->
      $scope.cluster_stats = stats[$routeParams.cluster_name]

    cluster_stats_query.error (e) ->
      $scope.errors.push e

    cluster_config_query.success (config) ->
      $scope.cluster_config = config[$routeParams.cluster_name]

    cluster_config_query.error (e) ->
      $scope.errors.push e

]


##{{COL_FIELD.map(function(node) { return ("<a href=\"/clusters/" + row.getProperty("cluster") + "/" + node + "\">" + node + "</a>") }).join(", ")}}