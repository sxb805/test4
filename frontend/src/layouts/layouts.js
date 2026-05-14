import { Outlet } from 'umi';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import advancedFormat from 'dayjs/plugin/advancedFormat';
import isMoment from 'dayjs/plugin/isMoment';
import localeData from 'dayjs/plugin/localeData';
import weekday from 'dayjs/plugin/weekday';
import weekOfYear from 'dayjs/plugin/weekOfYear';
import weekYear from 'dayjs/plugin/weekYear';
dayjs.locale('zh-cn');
dayjs.extend(localeData);
dayjs.extend(weekday);
dayjs.extend(isMoment);
dayjs.extend(weekOfYear);
dayjs.extend(weekYear);
dayjs.extend(advancedFormat);

export default function Layout(props) {
    return <Outlet/>;
}
