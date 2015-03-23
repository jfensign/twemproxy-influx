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
      enableFiltering: true
      columnDefs: [
        {name: 'cluster'}
        {name: 'client_connections'}
        {name: 'client_err'}
      ]
      onRegisterApi: (gridApi) ->
        $scope.gridApi = gridApi

    cluster_stats_query = do $cluster_stats.list

    cluster_stats_query.success (stats) ->
      $scope.cluster_stats = stats
      $scope.cluster_list_grid_configuration.data = _.filter _.map stats, (stat, cluster) -> _.extend {cluster: cluster}, _.pick stat, ['client_connections', 'client_err'] if _.isObject stat


    cluster_stats_query.error (e) ->
      $scope.errors.push e

]


Controllers.controller 'ClusterSelectCtrl', [
  '$scope'
  '$rootScope'
  '$location'
  '$log'
  ($scope, $rootScope, $location, $log) -> ]