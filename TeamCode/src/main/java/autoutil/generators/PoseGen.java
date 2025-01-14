package autoutil.generators;

import automodules.stage.Stage;
import autoutil.reactors.Reactor;
import geometry.position.Pose;

public class PoseGen extends Generator{
    @Override
    public void add(Pose start, Pose target) {}

    @Override
    public Stage getStage(Reactor reactor) {
        return new Stage(reactor.initialTarget(), reactor.mainTarget(this), reactor.exitTarget(), reactor.stopTarget());
    }
}