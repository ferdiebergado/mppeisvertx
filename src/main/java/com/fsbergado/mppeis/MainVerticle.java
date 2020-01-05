package com.fsbergado.mppeis;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Promise<String> schoolServiceVerticleDeployment = Promise.promise(); // <1>
    
    vertx.deployVerticle(new SchoolServiceVerticle(), schoolServiceVerticleDeployment); // <2>

    schoolServiceVerticleDeployment.future().compose(id -> { // <3>

      Promise<String> httpVerticleDeployment = Promise.promise();
      vertx.deployVerticle(new SchoolAPIVerticle(), // <4>
          new DeploymentOptions(), // <5>
          httpVerticleDeployment);

      return httpVerticleDeployment.future(); // <6>

    }).setHandler(ar -> { // <7>
      if (ar.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }
}
