export class ProvenanceList {
  provenanceList:Provenance[] = [];
}

export class Provenance {
  execution_time: string;
  agents : Agent[]
  activities: Activity[]
  entities: Entity[]
}

export interface Provenance1 {
  id: string;
  execution_time: string;
  agents: Agent[]
  activities: Activity[]
  entities: Entity[]
}

export class Agent {
  name:string
  values: Map<string, string>
}

export class Activity {
  name:string
  values: Map<string, string>

  get_value(key) {
    return this.values.get(key)
  }
}

export class Entity {
  name:string
  values: Map<string, string> = new Map<string, string>()

  get_value(key) {
    return this.values.get(key)
  }
}


