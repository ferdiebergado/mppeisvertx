package com.fsbergado.mppeis;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Promise<String> schoolServiceVerticleDeployment = Promise.promise(); // <1>

    vertx.deployVerticle(new SchoolServiceVerticle(), schoolServiceVerticleDeployment);

    schoolServiceVerticleDeployment.future().compose(id -> { // <3>

      Promise<String> teacherServiceVerticleDeployment = Promise.promise();
      vertx.deployVerticle(new TeacherServiceVerticle(), // <4>
          new DeploymentOptions(), // <5>
          teacherServiceVerticleDeployment);

      return teacherServiceVerticleDeployment.future(); // <6>

    }).setHandler(ar -> {
      if (ar.succeeded()) {
        vertx.deployVerticle(new HttpServerVerticle(), ar2 -> {
          if (ar2.succeeded()) {
            startPromise.complete();
          } else {
            startPromise.fail(ar2.cause());
          }
        }); // <2>
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }
}
