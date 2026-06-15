import { client } from "@/utils/client.ts";
import { ListenerScript, ListenerScriptsResponse, ListenerScriptWithProjects } from "@/interfaces/listener.ts";
import { DeleteResult } from "luna";

class ListenerService {
  getAll() {
    return client.get<ListenerScriptsResponse>('/gravity/listener/scripts');
  }

  get(id: number) {
    return client.get<ListenerScriptWithProjects>(`/gravity/listener/scripts/${id}`);
  }

  patch(id: number, script: Partial<ListenerScript>) {
    return client.patch<ListenerScriptWithProjects>(`/gravity/listener/scripts/${id}`, script);
  }

  delete(id: number) {
    return client.delete<DeleteResult>(`/gravity/listener/scripts/${id}`);
  }

  create(script: Partial<ListenerScript>) {
    return client.post<ListenerScriptWithProjects>(`/gravity/listener/scripts`, script);
  }
}

export const listenerService = new ListenerService();