public class DiffRequestStat {
    RequestStat refR;
    RequestStat challengerR;

    public DiffRequestStat(RequestStat refStat, RequestStat challengerStat) {
        refR = refStat;
        challengerR = challengerStat;
    }
}
