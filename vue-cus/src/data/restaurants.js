// 方法1: 直接導入圖片（推薦）
import restaurantImg from '@/assets/RestaurantPage/restaurant.avif'

// 餐廳資料庫 - 使用數字 ID 作為 key，與 timeslots_30d.json 的 restaurantId 對應
export const restaurants = {
    1: {
        id: 1,
        name: 'Plants',
        image: restaurantImg, // 使用導入的圖片
        address: '台北市大安區忠孝東路四段181巷40號',
        phone: '02-2771-2050',
        businessHours: '週一至週日 11:00–20:00',
        menuTitle: 'Plants 菜單',
        menuCategories: [{
                name: '主餐',
                items: [{
                        id: 101,
                        name: '香草烤雞胸',
                        description: '嫩烤雞胸配新鮮香草，搭配季節蔬菜和藜麥',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 420,
                        originalPrice: 480,
                        discountPrice: 420,
                        options: [{
                                id: 'side',
                                name: '配菜選擇',
                                type: 'radio',
                                items: [{
                                        id: 'quinoa',
                                        name: '藜麥沙拉',
                                        price: 0
                                    },
                                    {
                                        id: 'sweet_potato',
                                        name: '烤地瓜',
                                        price: 20
                                    },
                                    {
                                        id: 'pasta',
                                        name: '青醬義大利麵',
                                        price: 50
                                    }
                                ]
                            },
                            {
                                id: 'extras',
                                name: '額外加料',
                                type: 'checkbox',
                                items: [{
                                        id: 'avocado',
                                        name: '酪梨',
                                        price: 30
                                    },
                                    {
                                        id: 'egg',
                                        name: '水煮蛋',
                                        price: 20
                                    },
                                    {
                                        id: 'nuts',
                                        name: '堅果',
                                        price: 25
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        id: 102,
                        name: '彩虹蔬食碗',
                        description: '七彩時蔬配鷹嘴豆泥和塔希醬',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 350,
                        originalPrice: 380,
                        discountPrice: 350
                    },
                    {
                        id: 103,
                        name: '植物蛋白漢堡',
                        description: '植物肉餅配新鮮蔬菜和酪梨醬',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 390,
                        originalPrice: 420,
                        discountPrice: 390,
                        options: [{
                            id: 'bread',
                            name: '麵包選擇',
                            type: 'radio',
                            items: [{
                                    id: 'whole_wheat',
                                    name: '全麥麵包',
                                    price: 0
                                },
                                {
                                    id: 'sourdough',
                                    name: '酸種麵包',
                                    price: 15
                                },
                                {
                                    id: 'gluten_free',
                                    name: '無麩質麵包',
                                    price: 25
                                }
                            ]
                        }]
                    },
                    {
                        id: 104,
                        name: '鮭魚酪梨沙拉',
                        description: '新鮮鮭魚片配酪梨、堅果和芝麻醬',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 450,
                        originalPrice: 480,
                        discountPrice: 450
                    }
                ]
            },
            {
                name: '飲品',
                items: [{
                        id: 201,
                        name: '鮮榨柳橙汁',
                        description: '100% 新鮮柳橙現榨，無添加糖',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 120,
                        options: [{
                            id: 'size',
                            name: '容量選擇',
                            type: 'radio',
                            items: [{
                                    id: 'small',
                                    name: '小杯 (300ml)',
                                    price: 0
                                },
                                {
                                    id: 'large',
                                    name: '大杯 (500ml)',
                                    price: 30
                                }
                            ]
                        }]
                    },
                    {
                        id: 202,
                        name: '綠色蔬果汁',
                        description: '菠菜、芹菜、蘋果、檸檬調製',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 150,
                        originalPrice: 180,
                        discountPrice: 150
                    },
                    {
                        id: 203,
                        name: '有機花草茶',
                        description: '薄荷、洋甘菊、檸檬馬鞭草混合',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 90
                    }
                ]
            },
            {
                name: '甜點',
                items: [{
                        id: 301,
                        name: '免烤起司蛋糕',
                        description: '使用腰果和椰子油製作的純植物起司蛋糕',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 180,
                        originalPrice: 220,
                        discountPrice: 180
                    },
                    {
                        id: 302,
                        name: '巧克力能量球',
                        description: '椰棗、堅果、可可粉製作的健康甜點',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 80
                    }
                ]
            }
        ],
        copyrightText: '© 2021 Plants All Rights Reserved'
    },

    2: {
        id: 2,
        name: '海景餐廳',
        image: restaurantImg, // 暫時使用同一張圖片
        address: '台北市信義區信義路五段7號',
        phone: '02-2345-6789',
        businessHours: '週一至週日 10:00–22:00',
        menuTitle: '海景菜單',
        menuCategories: [{
                name: '海鮮',
                items: [{
                        id: 401,
                        name: '龍蝦義大利麵',
                        description: '新鮮龍蝦配奶油白醬義大利麵',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 880,
                        originalPrice: 980,
                        discountPrice: 880
                    },
                    {
                        id: 402,
                        name: '蒜蓉扇貝',
                        description: '新鮮扇貝配蒜蓉和香草奶油',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 680,
                        originalPrice: 750,
                        discountPrice: 680
                    },
                    {
                        id: 403,
                        name: '海鮮拼盤',
                        description: '龍蝦、扇貝、蝦子、蟹肉綜合拼盤',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 1280,
                        originalPrice: 1380,
                        discountPrice: 1280,
                        options: [{
                            id: 'portion',
                            name: '份量選擇',
                            type: 'radio',
                            items: [{
                                    id: 'single',
                                    name: '單人份',
                                    price: 0
                                },
                                {
                                    id: 'double',
                                    name: '雙人份',
                                    price: 600
                                }
                            ]
                        }]
                    }
                ]
            },
            {
                name: '牛排',
                items: [{
                        id: 501,
                        name: '安格斯牛排',
                        description: '頂級安格斯牛肉配薯泥和蔬菜',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 1200,
                        originalPrice: 1350,
                        discountPrice: 1200,
                        options: [{
                                id: 'doneness',
                                name: '熟度選擇',
                                type: 'radio',
                                items: [{
                                        id: 'rare',
                                        name: '三分熟',
                                        price: 0
                                    },
                                    {
                                        id: 'medium_rare',
                                        name: '五分熟',
                                        price: 0
                                    },
                                    {
                                        id: 'medium',
                                        name: '七分熟',
                                        price: 0
                                    },
                                    {
                                        id: 'well_done',
                                        name: '全熟',
                                        price: 0
                                    }
                                ]
                            },
                            {
                                id: 'sauce',
                                name: '醬汁選擇',
                                type: 'radio',
                                items: [{
                                        id: 'mushroom',
                                        name: '蘑菇醬',
                                        price: 0
                                    },
                                    {
                                        id: 'pepper',
                                        name: '胡椒醬',
                                        price: 0
                                    },
                                    {
                                        id: 'garlic',
                                        name: '蒜香奶油',
                                        price: 0
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        id: 502,
                        name: '戰斧牛排',
                        description: '900g 戰斧牛排配烤蔬菜',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 1800,
                        originalPrice: 2000,
                        discountPrice: 1800
                    }
                ]
            },
            {
                name: '素食',
                items: [{
                        id: 601,
                        name: '烤茄子帕馬森',
                        description: '義式烤茄子配帕馬森起司和番茄醬',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 380,
                        originalPrice: 420,
                        discountPrice: 380
                    },
                    {
                        id: 602,
                        name: '蔬菜燉飯',
                        description: '時蔬燉飯配松露油和帕馬森起司',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 450,
                        originalPrice: 480,
                        discountPrice: 450
                    }
                ]
            }
        ],
        copyrightText: '© 2021 海景餐廳 All Rights Reserved'
    },

    3: {
        id: 3,
        name: 'Italian Corner',
        image: restaurantImg, // 暫時使用同一張圖片
        address: '台北市中山區南京東路二段123號',
        phone: '02-3456-7890',
        businessHours: '週二至週日 11:30–21:30 (週一公休)',
        menuTitle: 'Italian Menu',
        menuCategories: [{
                name: 'Pizza',
                items: [{
                        id: 701,
                        name: 'Margherita Pizza',
                        description: '經典瑪格麗特披薩配新鮮羅勒和莫札瑞拉起司',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 380,
                        originalPrice: 420,
                        discountPrice: 380,
                        options: [{
                                id: 'size',
                                name: '尺寸選擇',
                                type: 'radio',
                                items: [{
                                        id: 'small',
                                        name: '小 (8吋)',
                                        price: 0
                                    },
                                    {
                                        id: 'medium',
                                        name: '中 (10吋)',
                                        price: 80
                                    },
                                    {
                                        id: 'large',
                                        name: '大 (12吋)',
                                        price: 150
                                    }
                                ]
                            },
                            {
                                id: 'toppings',
                                name: '額外配料',
                                type: 'checkbox',
                                items: [{
                                        id: 'mushroom',
                                        name: '蘑菇',
                                        price: 50
                                    },
                                    {
                                        id: 'pepperoni',
                                        name: '義式臘腸',
                                        price: 60
                                    },
                                    {
                                        id: 'olives',
                                        name: '橄欖',
                                        price: 40
                                    },
                                    {
                                        id: 'extra_cheese',
                                        name: '額外起司',
                                        price: 70
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        id: 702,
                        name: 'Quattro Stagioni',
                        description: '四季披薩配火腿、蘑菇、洋薊和橄欖',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 480,
                        originalPrice: 520,
                        discountPrice: 480
                    },
                    {
                        id: 703,
                        name: 'Prosciutto e Funghi',
                        description: '帕馬火腿配蘑菇和芝麻葉',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 520,
                        originalPrice: 580,
                        discountPrice: 520
                    }
                ]
            },
            {
                name: 'Pasta',
                items: [{
                        id: 801,
                        name: 'Spaghetti Carbonara',
                        description: '經典卡邦拉拉義大利麵配培根和帕馬森起司',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 350,
                        originalPrice: 380,
                        discountPrice: 350,
                        options: [{
                            id: 'pasta_type',
                            name: '麵條選擇',
                            type: 'radio',
                            items: [{
                                    id: 'spaghetti',
                                    name: '義大利麵條',
                                    price: 0
                                },
                                {
                                    id: 'penne',
                                    name: '筆管麵',
                                    price: 0
                                },
                                {
                                    id: 'fettuccine',
                                    name: '寬麵條',
                                    price: 10
                                }
                            ]
                        }]
                    },
                    {
                        id: 802,
                        name: 'Penne Arrabbiata',
                        description: '辣味番茄醬筆管麵配大蒜和辣椒',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 320,
                        originalPrice: 350,
                        discountPrice: 320
                    },
                    {
                        id: 803,
                        name: 'Fettuccine Alfredo',
                        description: '奶油白醬寬麵條配帕馬森起司',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 380,
                        originalPrice: 420,
                        discountPrice: 380
                    }
                ]
            },
            {
                name: 'Dessert',
                items: [{
                        id: 901,
                        name: 'Tiramisu',
                        description: '經典提拉米蘇配咖  啡和可可粉',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 180,
                        originalPrice: 220,
                        discountPrice: 180
                    },
                    {
                        id: 902,
                        name: 'Panna Cotta',
                        description: '義式奶酪配莓果醬',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 150,
                        originalPrice: 180,
                        discountPrice: 150
                    },
                    {
                        id: 903,
                        name: 'Cannoli',
                        description: '西西里島傳統甜點配瑞可塔起司',
                        image: 'https://img.ltn.com.tw/Upload/food/page/2018/02/01/180201-7131-0-C92C8.jpg',
                        price: 120,
                        originalPrice: 150,
                        discountPrice: 120
                    }
                ]
            }
        ],
        copyrightText: '© 2021 Italian Corner All Rights Reserved'
    }
}

// 根據 ID 獲取餐廳資料
export const getRestaurantById = (id) => {
    // 確保 ID 是數字類型
    const numericId = parseInt(id)
    return restaurants[numericId] || restaurants[1] // 預設返回 Plants (ID: 1)
}