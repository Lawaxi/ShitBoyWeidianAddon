# ShitboyWeidianAddon：微店补充

Mirai-Console插件，依赖[Lawaxi/Shitboy](https://github.com/Lawaxi/ShitBoy)使用

增加微店订单附加功能

## 使用

- 关于单个抽卡项目/单个PK项目的全部信息写入json中，以文本的形式向机器人提交
- 项目与id（见下）一一对应，修改、删除等操作时以id为索引，无需输入群号

~~~
/pk 新建 {"name":"美食盛宴","groups":[956084047],"item_id":6398900545,"opponents":[{"name":"陈蓁蓁","item_id":6395610121},{"name":"郑照暄","item_id":6395974667},{"name":"谢天依","item_id":6397705143}]}
~~~

单行json和多行json（prettyJson）效果相同，向机器人发送时建议发送单行

- 多行压缩单行工具：https://www.sojson.com/
- 语法检查和预览工具：https://tilipa.zlsam.com/json/

### 抽卡json格式（通过“/抽卡 新建 <json>”提交）

~~~json5
{"name":"","groups":[],"item_ids":[],"fee":"","qualities":[{"qlty":"","pr":0,"index":0,"gifts":[{"id":"","name":""}]}]}
~~~

~~~json5
{
  "id": "",
  //【可选】系统中记录关于此抽卡的独特id，已存在或未填写随机生成五位id
  "name": "",
  //抽卡名
  "groups": [12345,67890],
  //适用本抽卡的群（机器人需要加入所有群，在所有群都有管理员权限的账号才可以新建和修改）
  "item_ids": [12345],
  //适用本抽卡的商品id
  "fee": "15",
  //单抽金额/元（目前仅每个订单向下取整，多订单间不累计）
  "qualities": //不同卡的品质
  [
    {
      "qlty": "R",
      //显示名称
      "pr": 1,
      //此品质中单张卡的概率（整数）
      "index": 0,
      //品质的排行（越小越靠前）
      "gifts": //不同卡（每张卡需要有独特的id，显示名称随意）
      [
        {
          "id": "",
          "name": "",
          "pic": ""
        },
        {
          "id": "",
          "name": ""
        }
      ]
    }
  ]
}
~~~

示例：

~~~json5
{"name":"新陈代谢","groups":[817151561,588292517],"item_ids":[],"fee":"5","qualities":[{"qlty":"SSR","pr":5,"index":0,"gifts":[{"id":"couple","name":"新陈代谢"},{"id":"daughter","name":"新陈代谢的女儿"}]},{"qlty":"SR","pr":20,"index":1,"gifts":[{"id":"cl (1)","name":"英俊的陈琳"},{"id":"cl (2)","name":"没什么活力的陈琳"},{"id":"xty (1)","name":"青涩的谢天依"},{"id":"xty (2)","name":"我妈妈好大"}]},{"qlty":"R","pr":50,"index":3,"gifts":[{"id":"dog (1)","name":"可爱宁宁"},{"id":"dog (2)","name":"剪刀手宁宁"},{"id":"dog (3)","name":"面无表情的宁宁"},{"id":"dog (4)","name":"身穿二次元的宁宁"},{"id":"dog (6)","name":"加了很多贴纸的宁宁"},{"id":"dog (7)","name":"詹姆斯宁宁"},{"id":"dog (8)","name":"撅嘴的宁宁"},{"id":"dog (9)","name":"剪刀手宁宁2"},{"id":"dog (10)","name":"宁宁万圣节限定"},{"id":"dog (11)","name":"不那么可爱的宁宁"}]}]}
~~~

### PK json格式（通过“/抽卡 新建 <json>”提交）

~~~json5
{"name":"","groups":[],"item_id":0,"opponents":[{"name":"","item_id":[],"cookie":""}]}
~~~

~~~json5
{
  "id": "",
  //【可选】系统中记录关于此PK的独特id，已存在或未填写随机生成五位id
  "name": "",
  //pk名
  "groups": [12345,67890],
  //适用本抽卡的群（机器人需要加入所有群，在所有群都有管理员权限的账号才可以新建和修改）
  "item_id": 0,
  //PK链商品id（只支持单个商品，因为Shitboy播报是单个商品播报）
  "opponents": //对手信息（cookie选填，不填则使用库存相减计算金额）
  [
    {
      "name": "",
      "item_id": [0],
      "cookie": ""
    }
  ]
}
~~~

示例：

~~~json5
{"name":"美食盛宴","groups":[956084047],"item_id":6398900545,"opponents":[{"name":"陈蓁蓁","item_id":[6395610121]},{"name":"郑照暄","item_id":[6395974667]},{"name":"谢天依","item_id":[6397705143]}]}
~~~

## 更新日志

### 0.1.0

- alpha1 测试
- beta1 抽卡
- beta2 pk
- beta3 查卡
- test2 BUG修复：太开心了，mirai可以登录了
- test4 BUG修复（单个sku）
- test5 BUG修复（十倍金额）
- test6 对手增减库存时用真实数据修正
- test7 适配Shitboy017t20