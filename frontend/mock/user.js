import mockjs from 'mockjs';

const genList = num => {
    let data = [];
    for (let i = 0; i < num; i += 1) {
        data.push(
            mockjs.mock({
                id: '@guid',
                name: '@cname',
                email: '@email',
                'age|1-100': 50,
                gender: ['男', '女'][Math.floor(Math.random() * 2)],
            }),
        );
    }
    return data;
};

let dataSource = genList(36);

function page(req, res) {
    const { page, size, name, gender } = req.query;
    let filter = dataSource;
    if (name !== undefined && name !== null) {
        filter = filter.filter(item => item.name.includes(name));
    }
    if (gender !== undefined && gender !== null) {
        filter = filter.filter(item => item.gender === gender);
    }
    const list = filter.slice(page * size, page * size + Number(size));

    const result = {
        data: {
            rows: list,
            total: filter.length,
        },
        result: 0,
    };
    return res.json(result);
}

function add(req, res) {
    dataSource.unshift({
        id: '@guid',
        ...req.body,
    });
    const result = {
        result: 0,
        msg: '新增成功',
    };
    return res.json(result);
}

function edit(req, res) {
    dataSource = dataSource.map(item => {
        return item.id === req.body.id ? req.body : item;
    });
    const result = {
        data: req.body,
        result: 0,
        msg: '编辑成功',
    };
    return res.json(result);
}

function view(req, res) {
    const detail = dataSource.filter(item => item.id === req.query.id)[0];
    const result = {
        data: detail,
        result: 0,
        msg: '查询成功',
    };
    return res.json(result);
}

function batchDelete(req, res) {
    const { ids } = req.body;
    dataSource = dataSource.filter(item => !ids.includes(item.id));
    const result = {
        result: 0,
        msg: '删除成功',
    };
    return res.json(result);
}

export default {
    'GET /cloud/user/page': page,
    'POST /cloud/user/add': add,
    'POST /cloud/user/edit': edit,
    'GET /cloud/user/get': view,
    'POST /cloud/user/delete': batchDelete,
};
