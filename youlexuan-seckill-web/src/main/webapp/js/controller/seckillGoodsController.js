//控制层
	app.controller('seckillGoodsController' ,function($scope,seckillGoodsService){
		//读取列表数据绑定到表单中
		$scope.findList=function(){

			seckillGoodsService.findList().success(
				function(response){
					console.log(response);
					$scope.list=response;
				}
			);
		}
		//查询实体
		$scope.findOne=function(){
			seckillGoodsService.findOne($location.search()['id']).success(
				function(response){
					$scope.entity= response;
				}
			);
		}

	});
